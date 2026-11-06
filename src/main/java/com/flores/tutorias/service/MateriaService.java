package com.flores.tutorias.service;

import com.flores.tutorias.dto.MateriaForm;
import com.flores.tutorias.model.Carrera;
import com.flores.tutorias.model.Materia;
import com.flores.tutorias.model.Tag;
import com.flores.tutorias.repository.CarreraRepository;
import com.flores.tutorias.repository.MateriaRepository;
import com.flores.tutorias.repository.TagRepository;
import com.flores.tutorias.repository.TutoriaRepository;
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
public class MateriaService {

    private final MateriaRepository materiaRepo;
    private final CarreraRepository carreraRepo;
    private final TagRepository tagRepo;
    private final TutoriaRepository tutoriaRepo;

    public List<Materia> listar() {
        return materiaRepo.listarConCarreraYTags();
    }

    // Opciones del dropdown de carrera
    public List<Carrera> carreras() {
        return carreraRepo.findAll(Sort.by("nombre"));
    }

    public List<Tag> todosLosTags() {
        return tagRepo.findAll(Sort.by("nombre"));
    }

    public MateriaForm obtenerForm(Long id) {
        Materia m = materiaRepo.findById(id).orElseThrow();
        MateriaForm f = new MateriaForm();
        f.setId(m.getId());
        f.setNombre(m.getNombre());
        f.setDescripcion(m.getDescripcion());
        f.setCarreraId(m.getCarrera() != null ? m.getCarrera().getId() : null);
        f.setTagIds(m.getTags().stream().map(Tag::getId).collect(Collectors.toSet()));
        return f;
    }

    public void validarNegocio(MateriaForm form, BindingResult result) {
        // Si no tiene carrera escogida o escoge y se elimina desde otro lado, error
        if (form.getCarreraId() == null) {
            result.rejectValue("carreraId", "requerida", "Selecciona una carrera");
        } else if (!carreraRepo.existsById(form.getCarreraId())) {
            result.rejectValue("carreraId", "invalida", "La carrera seleccionada no existe");
        }

        if (!result.hasFieldErrors("nombre")) {
            String nombre = form.getNombre().trim();
            materiaRepo.findByNombreIgnoreCase(nombre).ifPresent(existente -> {
                if (!existente.getId().equals(form.getId())) {
                    result.rejectValue("nombre", "duplicado", "Ya existe una materia con ese nombre");
                }
            });
        }
    }

    @Transactional
    public void guardar(MateriaForm form) {
        Materia m = (form.getId() == null) ? new Materia() : materiaRepo.findById(form.getId()).orElseThrow();
        m.setNombre(form.getNombre().trim());
        m.setDescripcion(form.getDescripcion() == null || form.getDescripcion().isBlank() ? null : form.getDescripcion().trim());
        m.setCarrera(carreraRepo.getReferenceById(form.getCarreraId()));

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
        m.setTags(tags);
        materiaRepo.save(m);
    }

    @Transactional
    public String eliminar(Long id) {
        if (tutoriaRepo.existsByMateriaId(id)) {
            return "No se puede eliminar la materia porque tiene tutorías asociadas";
        }
        Materia m = materiaRepo.findById(id).orElse(null);
        if (m != null) {
            m.getTags().clear();
            materiaRepo.delete(m);
        }
        return null; // éxito
    }
}
