package sv.edu.udb.www.Cuponera.Entities;
// Generated 11-17-2018 03:23:21 PM by Hibernate Tools 4.3.1


import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

/**
 * Rubro generated by hbm2java
 */
@Entity
@Table(name="rubro"
    ,catalog="Cuponera"
)
public class Rubro  implements java.io.Serializable {


     private Integer id;
     @NotBlank(message="El nombre del rubro es obligatorio")
     private String descripcion;
     private Set<Empresa> empresas = new HashSet<Empresa>(0);

    public Rubro() {
    }

	
    public Rubro(String descripcion) {
        this.descripcion = descripcion;
    }
    public Rubro(String descripcion, Set<Empresa> empresas) {
       this.descripcion = descripcion;
       this.empresas = empresas;
    }
   
     @Id @GeneratedValue(strategy=IDENTITY)

    
    @Column(name="id", unique=true, nullable=false)
    public Integer getId() {
        return this.id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }

    
    @Column(name="descripcion", nullable=false, length=150)
    public String getDescripcion() {
        return this.descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

@OneToMany(fetch=FetchType.LAZY, mappedBy="rubro")
    public Set<Empresa> getEmpresas() {
        return this.empresas;
    }
    
    public void setEmpresas(Set<Empresa> empresas) {
        this.empresas = empresas;
    }




}


