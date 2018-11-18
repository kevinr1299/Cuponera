package sv.edu.udb.www.Cuponera.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import sv.edu.udb.www.Cuponera.Repository.RubroRepository;

@Controller
@RequestMapping("/rubro")
public class RubroController {
	
	@Autowired
	@Qualifier("RubroRepository")
	RubroRepository rubro;
	
	@GetMapping()
	public String listaRubros(Model model){
		model.addAttribute("lista", rubro.findAll());
		return "Rubro/Lista";
	}
}
