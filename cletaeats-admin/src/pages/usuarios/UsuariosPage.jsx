import { useEffect, useMemo, useState } from "react";
import Alert from "@mui/material/Alert";
import CircularProgress from "@mui/material/CircularProgress";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import InputAdornment from "@mui/material/InputAdornment";
import MenuItem from "@mui/material/MenuItem";
import TextField from "@mui/material/TextField";
import SearchRoundedIcon from "@mui/icons-material/SearchRounded";
import PowerSettingsNewRoundedIcon from "@mui/icons-material/PowerSettingsNewRounded";
import BlockRoundedIcon from "@mui/icons-material/BlockRounded";
import CheckCircleRoundedIcon from "@mui/icons-material/CheckCircleRounded";
import TwoWheelerRoundedIcon from "@mui/icons-material/TwoWheelerRounded";
import AddRoundedIcon from "@mui/icons-material/AddRounded";
import EditRoundedIcon from "@mui/icons-material/EditRounded";
import VisibilityRoundedIcon from "@mui/icons-material/VisibilityRounded";
import CloseRoundedIcon from "@mui/icons-material/CloseRounded";
import SaveRoundedIcon from "@mui/icons-material/SaveRounded";
import api from "../../api/axios";
import "./UsuariosPage.css";

const initialCreateForm = {
    rol: "CLIENTE",
    nombre: "",
    cedula: "",
    correo: "",
    telefono: "",
    password: "",
    direccionExacta: "",
    disponibilidad: "DISPONIBLE",
    fotoUrl: "",
};

const initialEditForm = {
    usuarioId: null,
    rol: "",
    nombre: "",
    cedula: "",
    correo: "",
    telefono: "",
    direccionExacta: "",
    disponibilidad: "DISPONIBLE",
    fotoUrl: "",
};

function estadoClase(estado) {
    switch (estado) {
        case "ACTIVO":
            return "app-chip app-chip--success";
        case "INACTIVO":
            return "app-chip app-chip--neutral";
        case "SUSPENDIDO":
            return "app-chip app-chip--danger";
        default:
            return "app-chip app-chip--neutral";
    }
}

function disponibilidadClase(disponibilidad) {
    return disponibilidad === "DISPONIBLE"
        ? "app-chip app-chip--success"
        : "app-chip app-chip--warning";
}

function getFotoUsuario(usuario, repartidor) {
    return usuario?.fotoUrl || repartidor?.fotoUrl || "";
}

export default function UsuariosPage() {
    const [usuarios, setUsuarios] = useState([]);
    const [clientesMap, setClientesMap] = useState({});
    const [repartidoresMap, setRepartidoresMap] = useState({});
    const [filtro, setFiltro] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [actionLoading, setActionLoading] = useState(false);

    const [confirmOpen, setConfirmOpen] = useState(false);
    const [confirmData, setConfirmData] = useState({
        title: "",
        message: "",
        actionType: "",
        payload: null,
    });

    const [createOpen, setCreateOpen] = useState(false);
    const [savingUser, setSavingUser] = useState(false);
    const [createForm, setCreateForm] = useState(initialCreateForm);

    const [editOpen, setEditOpen] = useState(false);
    const [savingEdit, setSavingEdit] = useState(false);
    const [editForm, setEditForm] = useState(initialEditForm);

    const [historialOpen, setHistorialOpen] = useState(false);
    const [historialLoading, setHistorialLoading] = useState(false);
    const [historialNombre, setHistorialNombre] = useState("");
    const [historialAmonestaciones, setHistorialAmonestaciones] = useState([]);

    useEffect(() => {
        let activo = true;

        const cargarTodo = async () => {
            try {
                const [usuariosRes, clientesRes, repartidoresRes] = await Promise.all([
                    api.get("/api/admin/usuarios"),
                    api.get("/api/admin/clientes"),
                    api.get("/api/admin/repartidores"),
                ]);

                if (!activo) return;

                setUsuarios(usuariosRes.data || []);

                const clientesObj = Object.fromEntries(
                    (clientesRes.data || []).map((cliente) => [cliente.usuarioId, cliente])
                );
                setClientesMap(clientesObj);

                const repartidoresObj = Object.fromEntries(
                    (repartidoresRes.data || []).map((repartidor) => [
                        repartidor.usuarioId,
                        repartidor,
                    ])
                );
                setRepartidoresMap(repartidoresObj);

                setError("");
            } catch (err) {
                if (!activo) return;
                setError(err.response?.data?.message || "No se pudieron cargar los usuarios");
            } finally {
                if (activo) setLoading(false);
            }
        };

        cargarTodo();

        return () => {
            activo = false;
        };
    }, []);

    const recargar = async () => {
        try {
            const [usuariosRes, clientesRes, repartidoresRes] = await Promise.all([
                api.get("/api/admin/usuarios"),
                api.get("/api/admin/clientes"),
                api.get("/api/admin/repartidores"),
            ]);

            setUsuarios(usuariosRes.data || []);

            const clientesObj = Object.fromEntries(
                (clientesRes.data || []).map((cliente) => [cliente.usuarioId, cliente])
            );
            setClientesMap(clientesObj);

            const repartidoresObj = Object.fromEntries(
                (repartidoresRes.data || []).map((repartidor) => [
                    repartidor.usuarioId,
                    repartidor,
                ])
            );
            setRepartidoresMap(repartidoresObj);

            setError("");
        } catch (err) {
            setError(err.response?.data?.message || "No se pudieron actualizar los datos");
        }
    };

    const usuariosFiltrados = useMemo(() => {
        const q = filtro.trim().toLowerCase();
        if (!q) return usuarios;

        return usuarios.filter((u) => {
            const cliente = clientesMap[u.usuarioId];
            const repartidor = repartidoresMap[u.usuarioId];

            return (
                (u.nombre || "").toLowerCase().includes(q) ||
                (u.correo || "").toLowerCase().includes(q) ||
                (u.cedula || "").toLowerCase().includes(q) ||
                (u.rol || "").toLowerCase().includes(q) ||
                (u.estado || "").toLowerCase().includes(q) ||
                (cliente?.direccionExacta || "").toLowerCase().includes(q) ||
                (repartidor?.disponibilidad || "").toLowerCase().includes(q)
            );
        });
    }, [usuarios, filtro, clientesMap, repartidoresMap]);

    const abrirConfirmacion = (title, message, actionType, payload) => {
        setConfirmData({ title, message, actionType, payload });
        setConfirmOpen(true);
    };

    const cerrarConfirmacion = () => {
        if (actionLoading) return;

        setConfirmOpen(false);
        setConfirmData({
            title: "",
            message: "",
            actionType: "",
            payload: null,
        });
    };

    const ejecutarAccion = async () => {
        const { actionType, payload } = confirmData;
        if (!actionType || !payload) return;

        setActionLoading(true);
        setError("");

        try {
            if (actionType === "toggle-estado-usuario") {
                await api.patch(`/api/admin/usuarios/${payload.usuarioId}/estado`, {
                    estado: payload.nuevoEstado,
                });
            }

            if (actionType === "toggle-suspension-cliente") {
                await api.patch(`/api/admin/clientes/${payload.clienteId}/suspension`, {
                    suspendido: payload.suspendido,
                });
            }

            if (actionType === "toggle-disponibilidad-repartidor") {
                await api.patch(
                    `/api/admin/repartidores/${payload.repartidorId}/disponibilidad`,
                    { disponibilidad: payload.nuevaDisponibilidad }
                );
            }

            await recargar();
            cerrarConfirmacion();
        } catch (err) {
            setError(err.response?.data?.message || "No se pudo ejecutar la acción");
        } finally {
            setActionLoading(false);
        }
    };

    const abrirCrearUsuario = () => {
        setCreateForm(initialCreateForm);
        setError("");
        setCreateOpen(true);
    };

    const cerrarCrearUsuario = () => {
        if (savingUser) return;

        setCreateOpen(false);
        setCreateForm(initialCreateForm);
    };

    const handleCreateChange = (field, value) => {
        setCreateForm((prev) => ({
            ...prev,
            [field]: value,
        }));
    };

    const guardarUsuario = async (e) => {
        e.preventDefault();
        setSavingUser(true);
        setError("");

        const payload = {
            rol: createForm.rol,
            nombre: createForm.nombre.trim(),
            cedula: createForm.cedula.trim(),
            correo: createForm.correo.trim().toLowerCase(),
            telefono: createForm.telefono.trim(),
            password: createForm.password.trim(),
            direccionExacta:
                createForm.rol === "CLIENTE" || createForm.rol === "REPARTIDOR"
                    ? createForm.direccionExacta.trim()
                    : null,
            disponibilidad:
                createForm.rol === "REPARTIDOR"
                    ? createForm.disponibilidad
                    : null,
            fotoUrl:
                createForm.rol === "REPARTIDOR"
                    ? createForm.fotoUrl.trim() || null
                    : null,
        };

        try {
            await api.post("/api/admin/usuarios", payload);
            await recargar();
            cerrarCrearUsuario();
        } catch (err) {
            setError(err.response?.data?.message || "No se pudo crear el usuario");
        } finally {
            setSavingUser(false);
        }
    };

    const abrirEditarUsuario = (usuario) => {
        const cliente = clientesMap[usuario.usuarioId];
        const repartidor = repartidoresMap[usuario.usuarioId];

        setEditForm({
            usuarioId: usuario.usuarioId,
            rol: usuario.rol,
            nombre: usuario.nombre || "",
            cedula: usuario.cedula || "",
            correo: usuario.correo || "",
            telefono: usuario.telefono || "",
            direccionExacta: cliente?.direccionExacta || repartidor?.direccionExacta || "",
            disponibilidad: repartidor?.disponibilidad || "DISPONIBLE",
            fotoUrl: getFotoUsuario(usuario, repartidor),
        });

        setError("");
        setEditOpen(true);
    };

    const cerrarEditarUsuario = () => {
        if (savingEdit) return;

        setEditOpen(false);
        setEditForm(initialEditForm);
    };

    const handleEditChange = (field, value) => {
        setEditForm((prev) => ({
            ...prev,
            [field]: value,
        }));
    };

    const actualizarUsuario = async (e) => {
        e.preventDefault();
        setSavingEdit(true);
        setError("");

        const payload = {
            nombre: editForm.nombre.trim(),
            cedula: editForm.cedula.trim(),
            correo: editForm.correo.trim().toLowerCase(),
            telefono: editForm.telefono.trim(),
            direccionExacta:
                editForm.rol === "CLIENTE" || editForm.rol === "REPARTIDOR"
                    ? editForm.direccionExacta.trim()
                    : null,
            disponibilidad:
                editForm.rol === "REPARTIDOR"
                    ? editForm.disponibilidad
                    : null,
            fotoUrl:
                editForm.rol === "REPARTIDOR"
                    ? editForm.fotoUrl.trim() || null
                    : null,
        };

        try {
            await api.put(`/api/admin/usuarios/${editForm.usuarioId}`, payload);
            await recargar();
            cerrarEditarUsuario();
        } catch (err) {
            setError(err.response?.data?.message || "No se pudo actualizar el usuario");
        } finally {
            setSavingEdit(false);
        }
    };

    const subirFotoUsuario = async (file, target) => {
        if (!file) return;

        const formData = new FormData();
        formData.append("file", file);

        try {
            if (target === "create") {
                setSavingUser(true);
            } else {
                setSavingEdit(true);
            }

            setError("");

            const response = await api.post(
                "/api/admin/uploads/imagen",
                formData,
                {
                    headers: {
                        "Content-Type": "multipart/form-data",
                    },
                }
            );

            const url = response.data?.url || "";

            if (target === "create") {
                setCreateForm((prev) => ({
                    ...prev,
                    fotoUrl: url,
                }));
            } else {
                setEditForm((prev) => ({
                    ...prev,
                    fotoUrl: url,
                }));
            }
        } catch (err) {
            setError(err.response?.data?.message || "No se pudo subir la foto");
        } finally {
            if (target === "create") {
                setSavingUser(false);
            } else {
                setSavingEdit(false);
            }
        }
    };

    const formatoFechaAmonestacion = (valor) => {
        if (!valor) return "—";
        return new Date(valor).toLocaleString("es-CR");
    };

    const abrirHistorialAmonestaciones = async (repartidor, usuarioNombre) => {
        try {
            setHistorialLoading(true);
            setHistorialNombre(usuarioNombre || "");
            setHistorialOpen(true);

            const response = await api.get(
                `/api/admin/repartidores/${repartidor.repartidorId}/amonestaciones`
            );

            setHistorialAmonestaciones(response.data || []);
            setError("");
        } catch (err) {
            setError(err.response?.data?.message || "No se pudieron cargar las amonestaciones");
            setHistorialAmonestaciones([]);
        } finally {
            setHistorialLoading(false);
        }
    };

    const cerrarHistorialAmonestaciones = () => {
        setHistorialOpen(false);
        setHistorialNombre("");
        setHistorialAmonestaciones([]);
    };

    const renderBadges = (usuario) => {
        const repartidor = repartidoresMap[usuario.usuarioId];

        return (
            <div className="usuarios-page__meta-badges">
                <span className="app-chip">{usuario.rol}</span>
                <span className={estadoClase(usuario.estado)}>{usuario.estado}</span>

                {repartidor && (
                    <span className={disponibilidadClase(repartidor.disponibilidad)}>
                        {repartidor.disponibilidad}
                    </span>
                )}
            </div>
        );
    };

    const renderAvatar = (usuario, repartidor) => {
        const fotoUrl = getFotoUsuario(usuario, repartidor);

        return (
            <div className="usuarios-page__avatar">
                {fotoUrl ? (
                    <img
                        src={fotoUrl}
                        alt={usuario.nombre || "Usuario"}
                        className="usuarios-page__avatar-image"
                        onError={(e) => {
                            e.currentTarget.style.display = "none";
                        }}
                    />
                ) : (
                    <span className="usuarios-page__avatar-placeholder">
                        {(usuario.nombre || "U").charAt(0).toUpperCase()}
                    </span>
                )}
            </div>
        );
    };

    const renderFotoForm = (form, target, onChange, disabled) => {
        return (
            <>
                <TextField
                    label="URL de foto"
                    value={form.fotoUrl}
                    onChange={(e) => onChange("fotoUrl", e.target.value)}
                    fullWidth
                    placeholder="https://..."
                />

                <label className="app-upload-dropzone">
                    <input
                        type="file"
                        accept="image/png,image/jpeg,image/webp"
                        hidden
                        disabled={disabled}
                        onChange={(e) => subirFotoUsuario(e.target.files?.[0], target)}
                    />

                    <span className="app-upload-dropzone__icon">＋</span>

                    <span className="app-upload-dropzone__title">
                        Subir foto del repartidor
                    </span>

                    <span className="app-upload-dropzone__hint">
                        PNG, JPG o WEBP
                    </span>
                </label>

                <div className="usuarios-page__photo-preview">
                    {form.fotoUrl ? (
                        <img
                            src={form.fotoUrl}
                            alt="Foto del repartidor"
                            className="usuarios-page__photo"
                        />
                    ) : (
                        <span className="usuarios-page__photo-placeholder">
                            Sin foto
                        </span>
                    )}
                </div>
            </>
        );
    };

    const renderAcciones = (usuario) => {
        const cliente = clientesMap[usuario.usuarioId];
        const repartidor = repartidoresMap[usuario.usuarioId];

        const nuevoEstado = usuario.estado === "ACTIVO" ? "INACTIVO" : "ACTIVO";
        const mostrarBotonEstadoGeneral = usuario.estado !== "SUSPENDIDO";

        return (
            <div className="usuarios-page__item-actions">
                <button
                    type="button"
                    className="usuarios-page__icon-button usuarios-page__icon-button--accent"
                    title="Editar"
                    aria-label="Editar"
                    onClick={() => abrirEditarUsuario(usuario)}
                >
                    <EditRoundedIcon fontSize="small" />
                </button>

                {mostrarBotonEstadoGeneral && (
                    <button
                        type="button"
                        className={`usuarios-page__icon-button ${
                            usuario.estado === "ACTIVO"
                                ? "usuarios-page__icon-button--warning"
                                : "usuarios-page__icon-button--success"
                        }`}
                        title={usuario.estado === "ACTIVO" ? "Inactivar" : "Activar"}
                        aria-label={usuario.estado === "ACTIVO" ? "Inactivar" : "Activar"}
                        onClick={() =>
                            abrirConfirmacion(
                                usuario.estado === "ACTIVO"
                                    ? "Inactivar usuario"
                                    : "Activar usuario",
                                `Se cambiará el estado de ${usuario.nombre} a ${nuevoEstado}.`,
                                "toggle-estado-usuario",
                                {
                                    usuarioId: usuario.usuarioId,
                                    nuevoEstado,
                                }
                            )
                        }
                    >
                        <PowerSettingsNewRoundedIcon fontSize="small" />
                    </button>
                )}

                {cliente && (
                    <button
                        type="button"
                        className={`usuarios-page__icon-button ${
                            usuario.estado === "SUSPENDIDO"
                                ? "usuarios-page__icon-button--success"
                                : "usuarios-page__icon-button--danger"
                        }`}
                        title={
                            usuario.estado === "SUSPENDIDO"
                                ? "Reactivar cliente"
                                : "Suspender cliente"
                        }
                        aria-label={
                            usuario.estado === "SUSPENDIDO"
                                ? "Reactivar cliente"
                                : "Suspender cliente"
                        }
                        onClick={() =>
                            abrirConfirmacion(
                                usuario.estado === "SUSPENDIDO"
                                    ? "Reactivar cliente"
                                    : "Suspender cliente",
                                usuario.estado === "SUSPENDIDO"
                                    ? `Se reactivará el cliente ${usuario.nombre}.`
                                    : `Se suspenderá el cliente ${usuario.nombre}.`,
                                "toggle-suspension-cliente",
                                {
                                    clienteId: cliente.clienteId,
                                    suspendido: usuario.estado !== "SUSPENDIDO",
                                }
                            )
                        }
                    >
                        {usuario.estado === "SUSPENDIDO" ? (
                            <CheckCircleRoundedIcon fontSize="small" />
                        ) : (
                            <BlockRoundedIcon fontSize="small" />
                        )}
                    </button>
                )}

                {repartidor && (
                    <>
                        <button
                            type="button"
                            className="usuarios-page__icon-button usuarios-page__icon-button--info"
                            title="Ver amonestaciones"
                            aria-label="Ver amonestaciones"
                            onClick={() =>
                                abrirHistorialAmonestaciones(repartidor, usuario.nombre)
                            }
                        >
                            <VisibilityRoundedIcon fontSize="small" />
                        </button>

                        <button
                            type="button"
                            className={`usuarios-page__icon-button ${
                                usuario.estado === "SUSPENDIDO"
                                    ? "usuarios-page__icon-button--success"
                                    : "usuarios-page__icon-button--danger"
                            }`}
                            title={
                                usuario.estado === "SUSPENDIDO"
                                    ? "Reactivar repartidor"
                                    : "Suspender repartidor"
                            }
                            aria-label={
                                usuario.estado === "SUSPENDIDO"
                                    ? "Reactivar repartidor"
                                    : "Suspender repartidor"
                            }
                            onClick={() =>
                                abrirConfirmacion(
                                    usuario.estado === "SUSPENDIDO"
                                        ? "Reactivar repartidor"
                                        : "Suspender repartidor",
                                    usuario.estado === "SUSPENDIDO"
                                        ? `Se reactivará el repartidor ${usuario.nombre}.`
                                        : `Se suspenderá el repartidor ${usuario.nombre}.`,
                                    "toggle-estado-usuario",
                                    {
                                        usuarioId: usuario.usuarioId,
                                        nuevoEstado:
                                            usuario.estado === "SUSPENDIDO"
                                                ? "ACTIVO"
                                                : "SUSPENDIDO",
                                    }
                                )
                            }
                        >
                            {usuario.estado === "SUSPENDIDO" ? (
                                <CheckCircleRoundedIcon fontSize="small" />
                            ) : (
                                <BlockRoundedIcon fontSize="small" />
                            )}
                        </button>

                        <button
                            type="button"
                            className="usuarios-page__icon-button usuarios-page__icon-button--info"
                            title={
                                repartidor.disponibilidad === "DISPONIBLE"
                                    ? "Marcar ocupado"
                                    : "Marcar disponible"
                            }
                            aria-label={
                                repartidor.disponibilidad === "DISPONIBLE"
                                    ? "Marcar ocupado"
                                    : "Marcar disponible"
                            }
                            onClick={() =>
                                abrirConfirmacion(
                                    "Cambiar disponibilidad",
                                    `Se cambiará la disponibilidad de ${usuario.nombre} a ${
                                        repartidor.disponibilidad === "DISPONIBLE"
                                            ? "OCUPADO"
                                            : "DISPONIBLE"
                                    }.`,
                                    "toggle-disponibilidad-repartidor",
                                    {
                                        repartidorId: repartidor.repartidorId,
                                        nuevaDisponibilidad:
                                            repartidor.disponibilidad === "DISPONIBLE"
                                                ? "OCUPADO"
                                                : "DISPONIBLE",
                                    }
                                )
                            }
                        >
                            <TwoWheelerRoundedIcon fontSize="small" />
                        </button>
                    </>
                )}
            </div>
        );
    };

    return (
        <div className="usuarios-page">
            <div className="usuarios-page__header usuarios-page__header--actions">
                <div>
                    <h1 className="usuarios-page__title">Usuarios</h1>
                    <p className="usuarios-page__subtitle">
                        Gestión general de cuentas del sistema
                    </p>
                </div>

                <button
                    type="button"
                    className="usuarios-page__top-icon-button usuarios-page__top-icon-button--primary"
                    onClick={abrirCrearUsuario}
                    title="Nuevo usuario"
                    aria-label="Nuevo usuario"
                >
                    <AddRoundedIcon fontSize="small" />
                </button>
            </div>

            <div className="usuarios-page__search-card">
                <div className="usuarios-page__search-content">
                    <TextField
                        fullWidth
                        placeholder="Buscar"
                        value={filtro}
                        onChange={(e) => setFiltro(e.target.value)}
                        slotProps={{
                            input: {
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <SearchRoundedIcon />
                                    </InputAdornment>
                                ),
                            },
                        }}
                    />
                </div>
            </div>

            {loading && (
                <div className="usuarios-page__loader">
                    <CircularProgress />
                </div>
            )}

            {!loading && error && <Alert severity="error">{error}</Alert>}

            {!loading && !error && (
                <div className="usuarios-page__list">
                    {usuariosFiltrados.map((usuario) => {
                        const cliente = clientesMap[usuario.usuarioId];
                        const repartidor = repartidoresMap[usuario.usuarioId];

                        return (
                            <div className="usuarios-page__item" key={usuario.usuarioId}>
                                <div className="usuarios-page__item-content">
                                    {renderAvatar(usuario, repartidor)}

                                    <div className="usuarios-page__item-main">
                                        <h3 className="usuarios-page__item-name">
                                            {usuario.nombre}
                                        </h3>

                                        <p className="usuarios-page__item-line">
                                            {usuario.correo}
                                        </p>

                                        <p className="usuarios-page__item-line">
                                            {usuario.cedula} · {usuario.telefono}
                                        </p>

                                        {cliente && (
                                            <p className="usuarios-page__item-line">
                                                {cliente.direccionExacta}
                                            </p>
                                        )}

                                        {repartidor && (
                                            <p className="usuarios-page__item-line">
                                                Km del día:{" "}
                                                {repartidor.kilometrosRecorridosDia} ·
                                                Amonestaciones:{" "}
                                                {repartidor.amonestacionesActivas}
                                            </p>
                                        )}

                                        {renderBadges(usuario)}
                                    </div>

                                    {renderAcciones(usuario)}
                                </div>
                            </div>
                        );
                    })}

                    {!usuariosFiltrados.length && (
                        <div className="usuarios-page__empty">
                            No hay usuarios que coincidan con la búsqueda.
                        </div>
                    )}
                </div>
            )}

            <Dialog open={confirmOpen} onClose={cerrarConfirmacion} maxWidth="xs" fullWidth>
                <DialogTitle>{confirmData.title}</DialogTitle>

                <DialogContent dividers>
                    <p className="usuarios-page__dialog-text">{confirmData.message}</p>
                </DialogContent>

                <DialogActions>
                    <button
                        type="button"
                        className="usuarios-page__dialog-icon-button usuarios-page__dialog-icon-button--secondary"
                        onClick={cerrarConfirmacion}
                        title="Cancelar"
                        aria-label="Cancelar"
                    >
                        <CloseRoundedIcon fontSize="small" />
                    </button>

                    <button
                        type="button"
                        className="usuarios-page__dialog-icon-button usuarios-page__dialog-icon-button--primary"
                        onClick={ejecutarAccion}
                        disabled={actionLoading}
                        title="Confirmar"
                        aria-label="Confirmar"
                    >
                        <SaveRoundedIcon fontSize="small" />
                    </button>
                </DialogActions>
            </Dialog>

            <Dialog open={createOpen} onClose={cerrarCrearUsuario} maxWidth="sm" fullWidth>
                <form onSubmit={guardarUsuario}>
                    <DialogTitle>Nuevo usuario</DialogTitle>

                    <DialogContent dividers>
                        <div className="usuarios-page__form">
                            <TextField
                                select
                                label="Rol"
                                value={createForm.rol}
                                onChange={(e) =>
                                    handleCreateChange("rol", e.target.value)
                                }
                                fullWidth
                            >
                                <MenuItem value="ADMIN">ADMIN</MenuItem>
                                <MenuItem value="CLIENTE">CLIENTE</MenuItem>
                                <MenuItem value="REPARTIDOR">REPARTIDOR</MenuItem>
                            </TextField>

                            <TextField
                                label="Nombre completo"
                                value={createForm.nombre}
                                onChange={(e) =>
                                    handleCreateChange("nombre", e.target.value)
                                }
                                fullWidth
                                required
                            />

                            <TextField
                                label="Cédula"
                                value={createForm.cedula}
                                onChange={(e) =>
                                    handleCreateChange("cedula", e.target.value)
                                }
                                fullWidth
                                required
                            />

                            <TextField
                                label="Correo"
                                value={createForm.correo}
                                onChange={(e) =>
                                    handleCreateChange("correo", e.target.value)
                                }
                                fullWidth
                                required
                            />

                            <TextField
                                label="Teléfono"
                                value={createForm.telefono}
                                onChange={(e) =>
                                    handleCreateChange("telefono", e.target.value)
                                }
                                fullWidth
                                required
                            />

                            <TextField
                                label="Contraseña"
                                type="password"
                                value={createForm.password}
                                onChange={(e) =>
                                    handleCreateChange("password", e.target.value)
                                }
                                fullWidth
                                required
                            />

                            {(createForm.rol === "CLIENTE" ||
                                createForm.rol === "REPARTIDOR") && (
                                <TextField
                                    label="Dirección exacta"
                                    value={createForm.direccionExacta}
                                    onChange={(e) =>
                                        handleCreateChange(
                                            "direccionExacta",
                                            e.target.value
                                        )
                                    }
                                    fullWidth
                                    required
                                />
                            )}

                            {createForm.rol === "REPARTIDOR" && (
                                <>
                                    <TextField
                                        select
                                        label="Disponibilidad"
                                        value={createForm.disponibilidad}
                                        onChange={(e) =>
                                            handleCreateChange(
                                                "disponibilidad",
                                                e.target.value
                                            )
                                        }
                                        fullWidth
                                    >
                                        <MenuItem value="DISPONIBLE">DISPONIBLE</MenuItem>
                                        <MenuItem value="OCUPADO">OCUPADO</MenuItem>
                                    </TextField>

                                    {renderFotoForm(
                                        createForm,
                                        "create",
                                        handleCreateChange,
                                        savingUser
                                    )}
                                </>
                            )}
                        </div>
                    </DialogContent>

                    <DialogActions>
                        <button
                            type="button"
                            className="usuarios-page__dialog-icon-button usuarios-page__dialog-icon-button--secondary"
                            onClick={cerrarCrearUsuario}
                            title="Cancelar"
                            aria-label="Cancelar"
                        >
                            <CloseRoundedIcon fontSize="small" />
                        </button>

                        <button
                            type="submit"
                            className="usuarios-page__dialog-icon-button usuarios-page__dialog-icon-button--primary"
                            disabled={savingUser}
                            title="Guardar"
                            aria-label="Guardar"
                        >
                            <SaveRoundedIcon fontSize="small" />
                        </button>
                    </DialogActions>
                </form>
            </Dialog>

            <Dialog open={editOpen} onClose={cerrarEditarUsuario} maxWidth="sm" fullWidth>
                <form onSubmit={actualizarUsuario}>
                    <DialogTitle>Editar usuario</DialogTitle>

                    <DialogContent dividers>
                        <div className="usuarios-page__form">
                            <TextField
                                label="Rol"
                                value={editForm.rol}
                                fullWidth
                                disabled
                            />

                            <TextField
                                label="Nombre completo"
                                value={editForm.nombre}
                                onChange={(e) =>
                                    handleEditChange("nombre", e.target.value)
                                }
                                fullWidth
                                required
                            />

                            <TextField
                                label="Cédula"
                                value={editForm.cedula}
                                onChange={(e) =>
                                    handleEditChange("cedula", e.target.value)
                                }
                                fullWidth
                                required
                            />

                            <TextField
                                label="Correo"
                                value={editForm.correo}
                                onChange={(e) =>
                                    handleEditChange("correo", e.target.value)
                                }
                                fullWidth
                                required
                            />

                            <TextField
                                label="Teléfono"
                                value={editForm.telefono}
                                onChange={(e) =>
                                    handleEditChange("telefono", e.target.value)
                                }
                                fullWidth
                                required
                            />

                            {(editForm.rol === "CLIENTE" ||
                                editForm.rol === "REPARTIDOR") && (
                                <TextField
                                    label="Dirección exacta"
                                    value={editForm.direccionExacta}
                                    onChange={(e) =>
                                        handleEditChange(
                                            "direccionExacta",
                                            e.target.value
                                        )
                                    }
                                    fullWidth
                                    required
                                />
                            )}

                            {editForm.rol === "REPARTIDOR" && (
                                <>
                                    <TextField
                                        select
                                        label="Disponibilidad"
                                        value={editForm.disponibilidad}
                                        onChange={(e) =>
                                            handleEditChange(
                                                "disponibilidad",
                                                e.target.value
                                            )
                                        }
                                        fullWidth
                                    >
                                        <MenuItem value="DISPONIBLE">DISPONIBLE</MenuItem>
                                        <MenuItem value="OCUPADO">OCUPADO</MenuItem>
                                    </TextField>

                                    {renderFotoForm(
                                        editForm,
                                        "edit",
                                        handleEditChange,
                                        savingEdit
                                    )}
                                </>
                            )}
                        </div>
                    </DialogContent>

                    <DialogActions>
                        <button
                            type="button"
                            className="usuarios-page__dialog-icon-button usuarios-page__dialog-icon-button--secondary"
                            onClick={cerrarEditarUsuario}
                            title="Cancelar"
                            aria-label="Cancelar"
                        >
                            <CloseRoundedIcon fontSize="small" />
                        </button>

                        <button
                            type="submit"
                            className="usuarios-page__dialog-icon-button usuarios-page__dialog-icon-button--primary"
                            disabled={savingEdit}
                            title="Guardar"
                            aria-label="Guardar"
                        >
                            <SaveRoundedIcon fontSize="small" />
                        </button>
                    </DialogActions>
                </form>
            </Dialog>

            <Dialog
                open={historialOpen}
                onClose={cerrarHistorialAmonestaciones}
                maxWidth="sm"
                fullWidth
            >
                <DialogTitle>Amonestaciones</DialogTitle>

                <DialogContent dividers>
                    <div className="usuarios-page__history">
                        {historialNombre && (
                            <p className="usuarios-page__history-title">
                                {historialNombre}
                            </p>
                        )}

                        {historialLoading && (
                            <div className="usuarios-page__loader">
                                <CircularProgress />
                            </div>
                        )}

                        {!historialLoading && !!historialAmonestaciones.length && (
                            <div className="usuarios-page__history-list">
                                {historialAmonestaciones.map((item) => (
                                    <div
                                        className="usuarios-page__history-card"
                                        key={item.amonestacionId}
                                    >
                                        <div className="usuarios-page__history-top">
                                            <span
                                                className={
                                                    item.activa === "S"
                                                        ? "app-chip app-chip--success"
                                                        : "app-chip app-chip--neutral"
                                                }
                                            >
                                                {item.activa === "S"
                                                    ? "ACTIVA"
                                                    : "INACTIVA"}
                                            </span>

                                            <span className="usuarios-page__history-date">
                                                {formatoFechaAmonestacion(
                                                    item.fechaAmonestacion
                                                )}
                                            </span>
                                        </div>

                                        <p className="usuarios-page__history-text">
                                            {item.motivo}
                                        </p>

                                        <p className="usuarios-page__history-admin">
                                            {item.adminNombre || "—"}
                                        </p>
                                    </div>
                                ))}
                            </div>
                        )}

                        {!historialLoading && !historialAmonestaciones.length && (
                            <div className="usuarios-page__empty">
                                Sin amonestaciones
                            </div>
                        )}
                    </div>
                </DialogContent>

                <DialogActions>
                    <button
                        type="button"
                        className="usuarios-page__dialog-icon-button usuarios-page__dialog-icon-button--secondary"
                        onClick={cerrarHistorialAmonestaciones}
                        title="Cerrar"
                        aria-label="Cerrar"
                    >
                        <CloseRoundedIcon fontSize="small" />
                    </button>
                </DialogActions>
            </Dialog>
        </div>
    );
}