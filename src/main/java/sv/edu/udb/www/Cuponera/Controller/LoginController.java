package sv.edu.udb.www.Cuponera.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class LoginController {
	
	@GetMapping("/login")
	public String showFormLogin() {
		return "/login";
	}
}
