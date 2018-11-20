package sv.edu.udb.www.Cuponera.Controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import sv.edu.udb.www.Cuponera.Entities.Rubro;
import sv.edu.udb.www.Cuponera.Repository.RubroRepository;

@Controller
@RequestMapping("/rubro")
public class RubroController {
	
	@Autowired
	@Qualifier("RubroRepository")
	RubroRepository rubroRepository;
	
	@GetMapping()
	public String listaRubros(Model model){
		model.addAttribute("lista", rubroRepository.findAll());
		return "Rubro/Lista";
	}
	
	@GetMapping("/nuevo")
	public String nuevoRubro(Model model) {
		model.addAttribute("rubro", new Rubro());
		return "Rubro/Nuevo";
	}
	
	@PostMapping("/nuevo")
	public String guardarRubro(@Valid @ModelAttribute("rubro") Rubro rubro, BindingResult result, Model model, RedirectAttributes atributos) {
		try {
			if(result.hasErrors()) {
				model.addAttribute("rubro", rubro);
				return "Rubro/Nuevo";
			}else {
				rubroRepository.save(rubro);
				atributos.addFlashAttribute("exito", "Rubro ingresado");
				return "redirect:/rubro";
			}
		}catch(Exception ex) {
			model.addAttribute("rubro", rubro);
			return "Rubro/Nuevo";
		}
	}
	
	@GetMapping("/{codigo}/actualizar")
	public String obtenerRubro(@PathVariable("codigo") int codigo, Model model) {
		model.addAttribute("rubro", rubroRepository.getOne(codigo));
		return "Rubro/Modificar";
	}
	
	@GetMapping("/{codigo}/eliminar")
	public String borrarRubro(@PathVariable("codigo") int codigo, Model model, RedirectAttributes atributos) {
		try {
			Rubro rubro = rubroRepository.getOne(codigo);
			rubroRepository.delete(rubro);
			atributos.addFlashAttribute("exito", "Rubro eliminado");
		}catch(Exception ex) {
			atributos.addFlashAttribute("fallo", "No se pudo eliminar el rubro");
		}
		return "redirect:/rubro";
		
	}
	
	@PostMapping("/modificar")
	public String actualizarRubro(@Valid @ModelAttribute("rubro") Rubro rubro, BindingResult result, Model model, RedirectAttributes atributos) {
		try {
			if(result.hasErrors()) {
				model.addAttribute("rubro", rubro);
				return "Rubro/Nuevo";
			}else {
				rubroRepository.save(rubro);
				atributos.addFlashAttribute("exito", "Rubro actualizado");
				return "redirect:/rubro";
			}
		}catch(Exception ex) {
			model.addAttribute("rubro", rubro);
			return "Rubro/Nuevo";
		}
	}
}
