package sv.edu.udb.www.Cuponera.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import sv.edu.udb.www.Cuponera.Entities.Usuario;

@Repository("UsuarioRepository")
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

	public abstract Usuario findByCorreo(String correo);
	@Query("SELECT MAX(u.id) FROM Usuario u")
	int getMaxId();
	public abstract Usuario findByToken(String token);
}
