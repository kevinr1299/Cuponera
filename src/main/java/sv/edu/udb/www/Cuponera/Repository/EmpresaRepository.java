package sv.edu.udb.www.Cuponera.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import sv.edu.udb.www.Cuponera.Entities.Empresa;

@Repository("EmpresaRepository")
public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {

	@Query("SELECT MAX(e.id) FROM Empresa e")
	int getMaxId();
	@Query("SELECT COUNT(e.id) FROM Empresa e")
	int getCount();
	
}
