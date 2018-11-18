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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Usuario generated by hbm2java
 */
@Entity
@Table(name="usuario"
    ,catalog="Cuponera"
    , uniqueConstraints = @UniqueConstraint(columnNames="correo") 
)
public class Usuario  implements java.io.Serializable {


     private Integer id;
     private TipoUsuario tipoUsuario;
     private String correo;
     private String password;
     private String token;
     private boolean verificado;
     private boolean inicio;
     private Set<Empleado> empleados = new HashSet<Empleado>(0);
     private Set<Cliente> clientes = new HashSet<Cliente>(0);

    public Usuario() {
    }

	
    public Usuario(TipoUsuario tipoUsuario, String correo, String password, String token, boolean verificado, boolean inicio) {
        this.tipoUsuario = tipoUsuario;
        this.correo = correo;
        this.password = password;
        this.token = token;
        this.verificado = verificado;
        this.inicio = inicio;
    }
    public Usuario(TipoUsuario tipoUsuario, String correo, String password, String token, boolean verificado, boolean inicio, Set<Empleado> empleados, Set<Cliente> clientes) {
       this.tipoUsuario = tipoUsuario;
       this.correo = correo;
       this.password = password;
       this.token = token;
       this.verificado = verificado;
       this.inicio = inicio;
       this.empleados = empleados;
       this.clientes = clientes;
    }
   
     @Id @GeneratedValue(strategy=IDENTITY)

    
    @Column(name="id", unique=true, nullable=false)
    public Integer getId() {
        return this.id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }

@ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="id_tipo", nullable=false)
    public TipoUsuario getTipoUsuario() {
        return this.tipoUsuario;
    }
    
    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    
    @Column(name="correo", unique=true, nullable=false, length=150)
    public String getCorreo() {
        return this.correo;
    }
    
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    
    @Column(name="password", nullable=false, length=64)
    public String getPassword() {
        return this.password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    
    @Column(name="token", nullable=false, length=64)
    public String getToken() {
        return this.token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }

    
    @Column(name="verificado", nullable=false)
    public boolean isVerificado() {
        return this.verificado;
    }
    
    public void setVerificado(boolean verificado) {
        this.verificado = verificado;
    }

    
    @Column(name="inicio", nullable=false)
    public boolean isInicio() {
        return this.inicio;
    }
    
    public void setInicio(boolean inicio) {
        this.inicio = inicio;
    }

@OneToMany(fetch=FetchType.LAZY, mappedBy="usuario")
    public Set<Empleado> getEmpleados() {
        return this.empleados;
    }
    
    public void setEmpleados(Set<Empleado> empleados) {
        this.empleados = empleados;
    }

@OneToMany(fetch=FetchType.LAZY, mappedBy="usuario")
    public Set<Cliente> getClientes() {
        return this.clientes;
    }
    
    public void setClientes(Set<Cliente> clientes) {
        this.clientes = clientes;
    }




}

