package cl.duoc.atenasapi.config;

import cl.duoc.atenasapi.domain.AfinidadPublico;
import cl.duoc.atenasapi.domain.AreaContacto;
import cl.duoc.atenasapi.domain.EstadoOportunidad;
import cl.duoc.atenasapi.domain.Fechas;
import cl.duoc.atenasapi.domain.MedioContacto;
import cl.duoc.atenasapi.domain.PotencialComercial;
import cl.duoc.atenasapi.domain.Prioridad;
import cl.duoc.atenasapi.domain.RolUsuario;
import cl.duoc.atenasapi.domain.TipoAccion;
import cl.duoc.atenasapi.domain.TipoOportunidad;
import cl.duoc.atenasapi.domain.TipoOrganizacion;
import cl.duoc.atenasapi.entity.Comuna;
import cl.duoc.atenasapi.entity.Contacto;
import cl.duoc.atenasapi.entity.Oportunidad;
import cl.duoc.atenasapi.entity.Organizacion;
import cl.duoc.atenasapi.entity.Region;
import cl.duoc.atenasapi.entity.Seguimiento;
import cl.duoc.atenasapi.entity.Usuario;
import cl.duoc.atenasapi.repository.ComunaRepository;
import cl.duoc.atenasapi.repository.ContactoRepository;
import cl.duoc.atenasapi.repository.OportunidadRepository;
import cl.duoc.atenasapi.repository.OrganizacionRepository;
import cl.duoc.atenasapi.repository.RegionRepository;
import cl.duoc.atenasapi.repository.SeguimientoRepository;
import cl.duoc.atenasapi.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Datos de prueba, los mismos que trae la app. Todas las organizaciones,
 * personas, correos y teléfonos son ficticios, según la restricción del caso.
 *
 * Se cargan una sola vez, cuando la base está vacía. Las fechas son relativas
 * a hoy, así la agenda y las alertas siempre tienen algo que mostrar.
 *
 * Clave de las cuatro cuentas: atenas123
 */
@Component
public class DatosSemilla implements CommandLineRunner {

    public static final String CLAVE_DEMO = "atenas123";

    private final UsuarioRepository usuarios;
    private final RegionRepository regiones;
    private final ComunaRepository comunas;
    private final OrganizacionRepository organizaciones;
    private final ContactoRepository contactos;
    private final OportunidadRepository oportunidades;
    private final SeguimientoRepository seguimientos;
    private final PasswordEncoder cifrador;

    private long hoy;
    private long ahora;

    public DatosSemilla(
        UsuarioRepository usuarios,
        RegionRepository regiones,
        ComunaRepository comunas,
        OrganizacionRepository organizaciones,
        ContactoRepository contactos,
        OportunidadRepository oportunidades,
        SeguimientoRepository seguimientos,
        PasswordEncoder cifrador
    ) {
        this.usuarios = usuarios;
        this.regiones = regiones;
        this.comunas = comunas;
        this.organizaciones = organizaciones;
        this.contactos = contactos;
        this.oportunidades = oportunidades;
        this.seguimientos = seguimientos;
        this.cifrador = cifrador;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarios.count() > 0) {
            return;
        }
        hoy = Fechas.hoy();
        ahora = Fechas.ahora();
        String clave = cifrador.encode(CLAVE_DEMO);

        // ---------- Usuarios ----------
        Usuario camila = usuario("cvaldes", clave, "Camila Valdés", RolUsuario.EJECUTIVO, "Convenios corporativos", "Región Metropolitana");
        Usuario rodrigo = usuario("rfuentes", clave, "Rodrigo Fuentes", RolUsuario.EJECUTIVO, "Convenios corporativos", "Valparaíso");
        Usuario paulina = usuario("psoto", clave, "Paulina Soto", RolUsuario.EJECUTIVO, "Convenios corporativos", "Biobío");
        Usuario marcelo = usuario("msilva", clave, "Marcelo Silva", RolUsuario.SUPERVISOR, "Jefatura comercial", "Nacional");

        // ---------- Catálogo de ubicación ----------
        Region rm = region("Región Metropolitana");
        Region valpo = region("Valparaíso");
        Region biobio = region("Biobío");

        Comuna santiago = comuna("Santiago", rm);
        Comuna puenteAlto = comuna("Puente Alto", rm);
        Comuna providencia = comuna("Providencia", rm);
        Comuna maipu = comuna("Maipú", rm);
        Comuna vinaDelMar = comuna("Viña del Mar", valpo);
        comuna("Valparaíso", valpo);
        Comuna concepcion = comuna("Concepción", biobio);
        Comuna talcahuano = comuna("Talcahuano", biobio);

        // ---------- Organizaciones ----------
        Organizacion vertice = organizacion("Constructora Vértice Austral", TipoOrganizacion.EMPRESA, "Construcción", santiago,
            "www.vertice-austral.cl", "Trabajadores de obra y oficina central", "Alta rotación de personal, interés en convenios dentales.");
        Organizacion cerroVerde = organizacion("Municipalidad de Cerro Verde", TipoOrganizacion.MUNICIPALIDAD, "Gobierno local", puenteAlto,
            "www.cerroverde.cl", "Vecinos y funcionarios municipales", "Realiza ferias de beneficios trimestrales.");
        Organizacion araucaria = organizacion("Instituto Previsional Araucaria", TipoOrganizacion.INSTITUCION_PRIVADA, "Previsión y bienestar", providencia,
            "www.araucaria-prev.cl", "Afiliados y sus familias", null);
        Organizacion expoSur = organizacion("Expo Ferias del Sur SpA", TipoOrganizacion.ORGANIZADOR_EVENTOS, "Producción de eventos", concepcion,
            "www.expoferiasdelsur.cl", "Expositores y público general", "Arrienda stands por metro cuadrado.");
        Organizacion riberas = organizacion("Centro Comercial Las Riberas", TipoOrganizacion.CENTRO_COMERCIAL, "Retail", vinaDelMar,
            "www.lasriberas.cl", "Visitantes de fin de semana", null);
        Organizacion agmAndes = organizacion("Asociación Gremial Metalúrgica Andes", TipoOrganizacion.ASOCIACION, "Gremial industrial", maipu,
            "www.agmandes.cl", "Empresas socias y sus trabajadores", "Jornada anual de bienestar en noviembre.");

        // ---------- Contactos ----------
        Contacto ignacia = contacto(vertice, "Ignacia Morales", "Jefa de Bienestar", AreaContacto.BIENESTAR,
            "imorales@vertice-austral.cl", "+56 9 7000 1122", MedioContacto.CORREO, dias(-12), null);
        Contacto tomas = contacto(vertice, "Tomás Riquelme", "Encargado de RR.HH.", AreaContacto.RECURSOS_HUMANOS,
            "triquelme@vertice-austral.cl", "+56 9 7000 1123", MedioContacto.TELEFONO, null, "Contacto alternativo.");
        Contacto valeria = contacto(cerroVerde, "Valeria Cáceres", "Coordinadora de Eventos", AreaContacto.EVENTOS,
            "vcaceres@cerroverde.cl", "+56 9 7100 2233", MedioContacto.CORREO, dias(-4), null);
        Contacto hector = contacto(araucaria, "Héctor Pizarro", "Jefe de Beneficios", AreaContacto.BENEFICIOS,
            "hpizarro@araucaria-prev.cl", "+56 9 7200 3344", MedioContacto.CORREO, dias(-20), null);
        Contacto daniela = contacto(expoSur, "Daniela Núñez", "Ejecutiva de Expositores", AreaContacto.ACTIVACIONES,
            "dnunez@expoferiasdelsur.cl", "+56 9 7300 4455", MedioContacto.WHATSAPP, dias(-2), null);
        Contacto sebastian = contacto(riberas, "Sebastián Lagos", "Jefe de Marketing", AreaContacto.MARKETING,
            "slagos@lasriberas.cl", "+56 9 7400 5566", MedioContacto.CORREO, null, "Responde solo en horario de oficina.");
        Contacto marcela = contacto(agmAndes, "Marcela Tapia", "Secretaria Ejecutiva", AreaContacto.ADMINISTRACION,
            "mtapia@agmandes.cl", "+56 9 7500 6677", MedioContacto.CORREO, dias(-35), null);
        Contacto jorge = contacto(cerroVerde, "Jorge Sandoval", "Director de Comunicaciones", AreaContacto.COMUNICACIONES,
            "jsandoval@cerroverde.cl", "+56 9 7100 2234", MedioContacto.PRESENCIAL, null, null);

        // ---------- Oportunidades ----------
        Oportunidad feriaPrimavera = oportunidad("Feria de Beneficios Municipal Primavera", TipoOportunidad.ACTIVIDAD_MUNICIPAL,
            "Feria abierta a vecinos con stands de salud, educación y convenios.", "Sitio web municipal",
            -18, 21, 21, 600, 1080, "Plaza Cívica s/n", puenteAlto, cerroVerde, valeria, camila, camila,
            "Vecinos y familias", 2500, Prioridad.ALTA, EstadoOportunidad.PROPUESTA_ENVIADA,
            PotencialComercial.ALTO, AfinidadPublico.ALTA, 180000.0, "Stand de 3x3 con mesa y dos sillas.",
            "Inscripción con 10 días de anticipación.", "12 stands de 3x3 en la plaza central", null);
        Oportunidad jornadaVertice = oportunidad("Jornada de Bienestar Vértice Austral", TipoOportunidad.ACTIVIDAD_TRABAJADORES,
            "Jornada interna para trabajadores de obra y oficina central.", "Contacto directo",
            -9, 12, null, 540, 840, "Av. Industrial 1450", santiago, vertice, ignacia, camila, camila,
            "Trabajadores de la constructora", 400, Prioridad.ALTA, EstadoOportunidad.EN_ESPERA_RESPUESTA,
            PotencialComercial.ALTO, AfinidadPublico.MEDIA, 0.0, "Sin costo, espacio facilitado por la empresa.",
            "Credencial de acceso para dos promotores.", "Un módulo en el casino de la empresa", null);
        Oportunidad expoBiobio = oportunidad("Expo Industrial Biobío 2026", TipoOportunidad.FERIA_EMPRESARIAL,
            "Feria industrial con expositores regionales y público general.", "Redes sociales del organizador",
            -30, 45, 47, 660, 1200, "Centro de Eventos Ribera Norte", concepcion, expoSur, daniela, paulina, paulina,
            "Trabajadores del sector industrial", 8000, Prioridad.ALTA, EstadoOportunidad.CONTACTO_REALIZADO,
            PotencialComercial.ALTO, AfinidadPublico.ALTA, 950000.0, "Arriendo por metro cuadrado, mínimo 9 m².",
            "Contrato firmado y pago del 50% por adelantado.", "Pabellón B, 40 stands; quedan 6 disponibles", null);
        oportunidad("Activación Fin de Semana Las Riberas", TipoOportunidad.ACTIVACION_COMERCIAL,
            "Módulo de captación en pasillo central del centro comercial.", "Prospección en terreno",
            -6, 30, 31, 720, 1260, "Av. Costanera 2300, nivel 1", vinaDelMar, riberas, sebastian, rodrigo, rodrigo,
            "Visitantes del centro comercial", 15000, Prioridad.MEDIA, EstadoOportunidad.CONTACTO_PENDIENTE,
            PotencialComercial.MEDIO, AfinidadPublico.MEDIA, 420000.0, null,
            "Seguro de responsabilidad civil.", null, null);
        Oportunidad feriaAraucaria = oportunidad("Feria de Afiliados Araucaria", TipoOportunidad.FERIA_INSTITUCIONAL,
            "Feria de beneficios para afiliados y sus familias.", "Newsletter institucional",
            -25, 8, null, 570, 1020, "Av. Nueva Providencia 1200", providencia, araucaria, hector, camila, marcelo,
            "Afiliados y familias", 1200, Prioridad.ALTA, EstadoOportunidad.CONFIRMADA,
            PotencialComercial.ALTO, AfinidadPublico.ALTA, 250000.0, "Stand asignado por la institución.",
            "Nómina de personal con 5 días de anticipación.", "Stand asignado en el hall principal", null);
        oportunidad("Jornada Anual de Bienestar AGM Andes", TipoOportunidad.JORNADA_BENEFICIOS,
            "Jornada para trabajadores de las empresas socias del gremio.", "Referencia de un cliente",
            -40, 60, null, 510, 960, "Camino a Melipilla 7800", maipu, agmAndes, marcela, null, marcelo,
            "Trabajadores de empresas socias", 900, Prioridad.MEDIA, EstadoOportunidad.IDENTIFICADA,
            PotencialComercial.MEDIO, AfinidadPublico.ALTA, 150000.0, null, null, null, "Falta asignar responsable.");
        Oportunidad feriaLaboral = oportunidad("Feria Laboral Municipal Invierno", TipoOportunidad.ACTIVIDAD_MUNICIPAL,
            "Feria de empleo y servicios con alta afluencia de público.", "Sitio web municipal",
            -70, -15, -15, 600, 1020, "Gimnasio Municipal", puenteAlto, cerroVerde, jorge, camila, camila,
            "Vecinos en búsqueda de empleo", 1800, Prioridad.MEDIA, EstadoOportunidad.ACTIVACION_REALIZADA,
            PotencialComercial.MEDIO, AfinidadPublico.MEDIA, 120000.0, null, null, null,
            "Activación ejecutada, pendiente registrar resultados.");
        Oportunidad encuentroVertice = oportunidad("Encuentro de Proveedores Vértice", TipoOportunidad.EVENTO_CORPORATIVO,
            "Encuentro cerrado con proveedores y contratistas.", "Contacto directo",
            -50, -5, null, 1080, 1260, "Hotel Centro Santiago", santiago, vertice, tomas, rodrigo, rodrigo,
            "Proveedores y contratistas", 150, Prioridad.BAJA, EstadoOportunidad.DESCARTADA,
            PotencialComercial.BAJO, AfinidadPublico.BAJA, 0.0, null, null, null,
            "Público no coincide con el objetivo de la agencia.");
        oportunidad("Feria Costumbrista Talcahuano", TipoOportunidad.ACTIVIDAD_MUNICIPAL,
            "Feria comunal con alta concentración de público familiar.", "Redes sociales municipales",
            -3, 38, 40, 660, 1320, "Costanera Talcahuano", talcahuano, expoSur, null, paulina, paulina,
            "Público familiar", 6000, Prioridad.BAJA, EstadoOportunidad.IDENTIFICADA,
            PotencialComercial.BAJO, AfinidadPublico.MEDIA, 90000.0, null, null, null,
            "Falta identificar el contacto responsable.");
        Oportunidad semanaAraucaria = oportunidad("Semana del Trabajador Araucaria", TipoOportunidad.ACTIVIDAD_TRABAJADORES,
            "Ciclo de actividades para funcionarios de la institución.", "Contacto directo",
            -14, 25, 29, 540, 1080, "Av. Nueva Providencia 1200", providencia, araucaria, hector, rodrigo, marcelo,
            "Funcionarios de la institución", 600, Prioridad.MEDIA, EstadoOportunidad.CONTACTO_REALIZADO,
            PotencialComercial.MEDIO, AfinidadPublico.BAJA, 75000.0, null, null, null, null);

        // ---------- Seguimientos ----------
        seguimiento(feriaPrimavera, camila, TipoAccion.PRIMER_CONTACTO, -15,
            "Se contactó a la coordinadora de eventos, pidió enviar propuesta formal.",
            "Enviar propuesta con valores", -10, EstadoOportunidad.CONTACTO_PENDIENTE, EstadoOportunidad.CONTACTO_REALIZADO, null);
        seguimiento(feriaPrimavera, camila, TipoAccion.ENVIO_PROPUESTA, -10,
            "Propuesta enviada por correo con dos alternativas de stand.",
            "Confirmar recepción y agendar reunión", -2, EstadoOportunidad.CONTACTO_REALIZADO, EstadoOportunidad.PROPUESTA_ENVIADA, null);
        seguimiento(jornadaVertice, camila, TipoAccion.ENVIO_PRESENTACION, -7,
            "Se envió presentación institucional a Bienestar.",
            "Llamar para confirmar interés", 2, EstadoOportunidad.CONTACTO_REALIZADO, EstadoOportunidad.EN_ESPERA_RESPUESTA, null);
        seguimiento(expoBiobio, paulina, TipoAccion.SOLICITUD_ANTECEDENTES, -5,
            "Solicitamos plano de stands y valores por metro cuadrado.",
            "Evaluar costo contra presupuesto", 5, EstadoOportunidad.CONTACTO_REALIZADO, EstadoOportunidad.CONTACTO_REALIZADO, null);
        seguimiento(feriaAraucaria, marcelo, TipoAccion.CONFIRMACION, -3,
            "La institución confirmó la participación de la agencia.",
            "Enviar nómina de personal", 3, EstadoOportunidad.EN_ESPERA_RESPUESTA, EstadoOportunidad.CONFIRMADA, null);
        seguimiento(feriaLaboral, camila, TipoAccion.CIERRE, -14,
            "Activación ejecutada, se generaron 62 contactos.",
            "Registrar resultado final", -7, EstadoOportunidad.CONFIRMADA, EstadoOportunidad.ACTIVACION_REALIZADA, null);
        seguimiento(encuentroVertice, rodrigo, TipoAccion.INVESTIGACION_INICIAL, -45,
            "Se revisó el perfil del evento, público no coincide con el objetivo.",
            null, null, EstadoOportunidad.IDENTIFICADA, EstadoOportunidad.DESCARTADA, "Descartada por el supervisor.");
        seguimiento(semanaAraucaria, rodrigo, TipoAccion.LLAMADA, -8,
            "Conversación con el jefe de beneficios, pidió propuesta en octubre.",
            "Preparar propuesta", -1, EstadoOportunidad.CONTACTO_REALIZADO, EstadoOportunidad.CONTACTO_REALIZADO, null);
    }

    // ---------- Constructores de datos ----------

    private long dias(int cantidad) {
        return Fechas.masDias(hoy, cantidad);
    }

    private Usuario usuario(String nombreUsuario, String claveHash, String nombre, RolUsuario rol, String area, String zona) {
        Usuario u = new Usuario();
        u.setNombreUsuario(nombreUsuario);
        u.setClaveHash(claveHash);
        u.setNombreCompleto(nombre);
        u.setRol(rol);
        u.setArea(area);
        u.setZona(zona);
        u.setActivo(true);
        u.setFechaCreacion(ahora);
        u.setFechaActualizacion(ahora);
        return usuarios.save(u);
    }

    private Region region(String nombre) {
        Region r = new Region();
        r.setNombre(nombre);
        return regiones.save(r);
    }

    private Comuna comuna(String nombre, Region region) {
        Comuna c = new Comuna();
        c.setNombre(nombre);
        c.setRegion(region);
        return comunas.save(c);
    }

    private Organizacion organizacion(
        String nombre, TipoOrganizacion tipo, String rubro, Comuna comuna,
        String sitioWeb, String publicoAsociado, String observaciones
    ) {
        Organizacion o = new Organizacion();
        o.setNombre(nombre);
        o.setTipo(tipo);
        o.setRubro(rubro);
        o.setComuna(comuna);
        o.setSitioWeb(sitioWeb);
        o.setPublicoAsociado(publicoAsociado);
        o.setObservaciones(observaciones);
        o.setFechaCreacion(ahora);
        return organizaciones.save(o);
    }

    private Contacto contacto(
        Organizacion organizacion, String nombre, String cargo, AreaContacto area, String correo,
        String telefono, MedioContacto medio, Long fechaUltimoContacto, String observaciones
    ) {
        Contacto c = new Contacto();
        c.setOrganizacion(organizacion);
        c.setNombre(nombre);
        c.setCargo(cargo);
        c.setArea(area);
        c.setCorreo(correo);
        c.setTelefono(telefono);
        c.setMedioPreferente(medio);
        c.setActivo(true);
        c.setFechaUltimoContacto(fechaUltimoContacto);
        c.setObservaciones(observaciones);
        return contactos.save(c);
    }

    private Oportunidad oportunidad(
        String nombre, TipoOportunidad tipo, String descripcion, String fuente,
        int diasDeteccion, int diasInicio, Integer diasTermino, int horaInicio, int horaTermino,
        String direccion, Comuna comuna, Organizacion organizacion, Contacto contacto,
        Usuario responsable, Usuario creador, String publicoObjetivo, int publicoEsperado,
        Prioridad prioridad, EstadoOportunidad estado, PotencialComercial potencial, AfinidadPublico afinidad,
        double costo, String condiciones, String requisitos, String espacios, String observaciones
    ) {
        Oportunidad o = new Oportunidad();
        o.setNombre(nombre);
        o.setTipo(tipo);
        o.setDescripcion(descripcion);
        o.setFuenteInformacion(fuente);
        o.setFechaDeteccion(dias(diasDeteccion));
        o.setFechaInicioEvento(dias(diasInicio));
        o.setFechaTerminoEvento(diasTermino == null ? null : dias(diasTermino));
        o.setHoraInicio(horaInicio);
        o.setHoraTermino(horaTermino);
        o.setDireccion(direccion);
        o.setComuna(comuna);
        o.setOrganizacion(organizacion);
        o.setContacto(contacto);
        o.setResponsable(responsable);
        o.setCreadoPor(creador);
        o.setPublicoObjetivo(publicoObjetivo);
        o.setPublicoEsperado(publicoEsperado);
        o.setPrioridad(prioridad);
        o.setEstado(estado);
        o.setPotencial(potencial);
        o.setAfinidad(afinidad);
        o.setCostoReferencial(costo);
        o.setCondiciones(condiciones);
        o.setRequisitos(requisitos);
        o.setEspaciosDisponibles(espacios);
        o.setObservaciones(observaciones);
        o.setFechaCreacion(ahora);
        o.setFechaActualizacion(ahora);
        return oportunidades.save(o);
    }

    private void seguimiento(
        Oportunidad oportunidad, Usuario usuario, TipoAccion tipo, int diasAccion, String resultado,
        String proximaAccion, Integer diasProximo, EstadoOportunidad anterior, EstadoOportunidad nuevo, String observaciones
    ) {
        Seguimiento s = new Seguimiento();
        s.setOportunidad(oportunidad);
        s.setUsuario(usuario);
        s.setTipoAccion(tipo);
        s.setFechaAccion(dias(diasAccion));
        s.setResultado(resultado);
        s.setProximaAccion(proximaAccion);
        s.setFechaProximoSeguimiento(diasProximo == null ? null : dias(diasProximo));
        s.setEstadoAnterior(anterior);
        s.setEstadoNuevo(nuevo);
        s.setObservaciones(observaciones);
        seguimientos.save(s);
    }
}
