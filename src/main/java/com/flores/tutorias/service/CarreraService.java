package com.flores.tutorias.service;

import com.flores.tutorias.dto.CarreraForm;
import com.flores.tutorias.model.Carrera;
import com.flores.tutorias.model.Tag;
import com.flores.tutorias.repository.CarreraRepository;
import com.flores.tutorias.repository.MateriaRepository;
import com.flores.tutorias.repository.TagRepository;
import com.flores.tutorias.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarreraService {

    private final CarreraRepository carreraRepo;
    private final TagRepository tagRepo;
    private final UsuarioRepository usuarioRepo;
    private final MateriaRepository materiaRepo;

    public List<Carrera> listar() {
        return carreraRepo.listarConTags();
    }

    public List<Tag> todosLosTags() {
        return tagRepo.findAll(Sort.by("nombre"));
    }

    public CarreraForm obtenerForm(Long id) {
        Carrera c = carreraRepo.findById(id).orElseThrow();
        CarreraForm f = new CarreraForm();
        f.setId(c.getId());
        f.setNombre(c.getNombre());
        f.setTagIds(c.getTags().stream().map(Tag::getId).collect(Collectors.toSet()));
        return f;
    }

    public void validarNegocio(CarreraForm form, BindingResult result) {
        if (!result.hasFieldErrors("nombre")) {
            String nombre = form.getNombre().trim();
            carreraRepo.findByNombreIgnoreCase(nombre).ifPresent(existente -> {
                if (!existente.getId().equals(form.getId())) {
                    result.rejectValue("nombre", "duplicado", "Ya existe una carrera con ese nombre");
                }
            });
        }
    }

    @Transactional
    public void guardar(CarreraForm form) {
        Carrera c = (form.getId() == null) ? new Carrera() : carreraRepo.findById(form.getId()).orElseThrow();
        c.setNombre(form.getNombre().trim());

        Set<Tag> tags = new HashSet<>();
        if (form.getTagIds() != null && !form.getTagIds().isEmpty()) {
            tags.addAll(tagRepo.findAllById(form.getTagIds()));
        }
        if (form.getNuevosTags() != null && !form.getNuevosTags().isBlank()) {
            for (String t : form.getNuevosTags().split("[,;|]")) {
                String norm = t.trim().toLowerCase();
                if (!norm.isBlank()) {
                    Tag tag = tagRepo.findByNombreIgnoreCase(norm).orElseGet(() -> {
                        Tag nuevo = new Tag();
                        nuevo.setNombre(norm);
                        return tagRepo.save(nuevo);
                    });
                    tags.add(tag);
                }
            }
        }
        c.setTags(tags);
        carreraRepo.save(c);
    }

    @Transactional
    public String eliminar(Long id) {
        if (usuarioRepo.existsByCarreraId(id)) {
            return "No se puede eliminar la carrera porque tiene usuarios asignados";
        }
        if (materiaRepo.existsByCarreraId(id)) {
            return "No se puede eliminar la carrera porque tiene materias asignadas";
        }
        Carrera c = carreraRepo.findById(id).orElse(null);
        if (c != null) {
            c.getTags().clear();
            carreraRepo.delete(c);
        }
        return null; // éxito
    }
}
