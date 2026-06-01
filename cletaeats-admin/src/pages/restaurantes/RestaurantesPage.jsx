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
import AddRoundedIcon from "@mui/icons-material/AddRounded";
import EditRoundedIcon from "@mui/icons-material/EditRounded";
import PowerSettingsNewRoundedIcon from "@mui/icons-material/PowerSettingsNewRounded";
import CloseRoundedIcon from "@mui/icons-material/CloseRounded";
import SaveRoundedIcon from "@mui/icons-material/SaveRounded";
import api from "../../api/axios";
import LocationPickerMap from "../../components/LocationPickerMap";
import "./RestaurantesPage.css";

const initialForm = {
    nombre: "",
    cedulaJuridica: "",
    direccion: "",
    latitud: "",
    longitud: "",
    tipoComida: "RAPIDA",
    imagenUrl: "",
};

const tiposComida = [
    { value: "RAPIDA", label: "Rápida" },
    { value: "HAMBURGUESAS", label: "Hamburguesas" },
    { value: "PIZZA", label: "Pizza" },
    { value: "POLLO", label: "Pollo" },
    { value: "CHINA", label: "China" },
    { value: "JAPONESA", label: "Japonesa" },
    { value: "ITALIANA", label: "Italiana" },
    { value: "MEXICANA", label: "Mexicana" },
    { value: "SALUDABLE", label: "Saludable" },
    { value: "POSTRES", label: "Postres" },
];

function estadoClase(estado) {
    return estado === "ACTIVO"
        ? "app-chip app-chip--success"
        : "app-chip app-chip--neutral";
}

export default function RestaurantesPage() {
    const [restaurantes, setRestaurantes] = useState([]);
    const [filtro, setFiltro] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [dialogOpen, setDialogOpen] = useState(false);
    const [saving, setSaving] = useState(false);
    const [editingItem, setEditingItem] = useState(null);
    const [form, setForm] = useState(initialForm);

    const [confirmOpen, setConfirmOpen] = useState(false);
    const [confirmItem, setConfirmItem] = useState(null);
    const [confirmLoading, setConfirmLoading] = useState(false);

    useEffect(() => {
        let activo = true;

        const cargarRestaurantes = async () => {
            try {
                const response = await api.get("/api/restaurantes");

                if (activo) {
                    setRestaurantes(response.data);
                    setError("");
                }
            } catch (err) {
                if (activo) {
                    setError(
                        err.response?.data?.message ||
                        "No se pudieron cargar los restaurantes"
                    );
                }
            } finally {
                if (activo) {
                    setLoading(false);
                }
            }
        };

        cargarRestaurantes();

        return () => {
            activo = false;
        };
    }, []);

    const restaurantesFiltrados = useMemo(() => {
        const q = filtro.trim().toLowerCase();

        if (!q) {
            return restaurantes;
        }

        return restaurantes.filter((r) => {
            return (
                String(r.nombre || "").toLowerCase().includes(q) ||
                String(r.cedulaJuridica || "").toLowerCase().includes(q) ||
                String(r.direccion || "").toLowerCase().includes(q) ||
                String(r.tipoComida || "").toLowerCase().includes(q) ||
                String(r.estado || "").toLowerCase().includes(q)
            );
        });
    }, [restaurantes, filtro]);

    const recargar = async () => {
        try {
            const response = await api.get("/api/restaurantes");
            setRestaurantes(response.data);
            setError("");
        } catch (err) {
            setError(
                err.response?.data?.message ||
                "No se pudieron cargar los restaurantes"
            );
        }
    };

    const abrirCrear = () => {
        setEditingItem(null);
        setForm(initialForm);
        setError("");
        setDialogOpen(true);
    };

    const abrirEditar = (item) => {
        setEditingItem(item);
        setForm({
            nombre: item.nombre || "",
            cedulaJuridica: item.cedulaJuridica || "",
            direccion: item.direccion || "",
            latitud: item.latitud ?? "",
            longitud: item.longitud ?? "",
            tipoComida: item.tipoComida || "RAPIDA",
            imagenUrl: item.imagenUrl || "",
        });
        setError("");
        setDialogOpen(true);
    };

    const cerrarDialog = () => {
        if (saving) {
            return;
        }

        setDialogOpen(false);
        setEditingItem(null);
        setForm(initialForm);
    };

    const abrirConfirmEstado = (item) => {
        setConfirmItem(item);
        setConfirmOpen(true);
    };

    const cerrarConfirmEstado = () => {
        if (confirmLoading) {
            return;
        }

        setConfirmOpen(false);
        setConfirmItem(null);
    };

    const handleChange = (field, value) => {
        setForm((prev) => ({
            ...prev,
            [field]: value,
        }));
    };

    const guardarRestaurante = async (e) => {
        e.preventDefault();
        setError("");

        const latitud = form.latitud === "" ? null : Number(form.latitud);
        const longitud = form.longitud === "" ? null : Number(form.longitud);

        const payload = {
            nombre: form.nombre.trim(),
            cedulaJuridica: form.cedulaJuridica.trim(),
            direccion: form.direccion.trim(),
            latitud,
            longitud,
            tipoComida: form.tipoComida || "RAPIDA",
            imagenUrl: form.imagenUrl.trim() || null,
        };

        if (!payload.nombre) {
            setError("Ingresá el nombre del restaurante.");
            return;
        }

        if (!payload.cedulaJuridica) {
            setError("Ingresá la cédula jurídica.");
            return;
        }

        if (!payload.direccion) {
            setError("Ingresá la dirección.");
            return;
        }

        if (!payload.tipoComida) {
            setError("Seleccioná un tipo de comida.");
            return;
        }

        if (
            latitud === null ||
            longitud === null ||
            Number.isNaN(latitud) ||
            Number.isNaN(longitud)
        ) {
            setError("Seleccioná una ubicación válida en el mapa.");
            return;
        }

        try {
            setSaving(true);

            if (editingItem) {
                await api.put(`/api/admin/restaurantes/${editingItem.id}`, payload);
            } else {
                await api.post("/api/admin/restaurantes", payload);
            }

            await recargar();
            cerrarDialog();
        } catch (err) {
            setError(
                err.response?.data?.message ||
                "No se pudo guardar el restaurante"
            );
        } finally {
            setSaving(false);
        }
    };

    const subirImagen = async (file) => {
        if (!file) {
            return;
        }

        const formData = new FormData();
        formData.append("file", file);

        try {
            setSaving(true);

            const response = await api.post(
                "/api/admin/uploads/imagen",
                formData,
                {
                    headers: {
                        "Content-Type": "multipart/form-data",
                    },
                }
            );

            handleChange("imagenUrl", response.data.url);
        } catch (err) {
            setError(
                err.response?.data?.message ||
                "No se pudo subir la imagen"
            );
        } finally {
            setSaving(false);
        }
    };

    const handleDropImage = (e) => {
        e.preventDefault();

        const file = e.dataTransfer.files?.[0];

        if (!file) {
            return;
        }

        subirImagen(file);
    };

    const handleDragOverImage = (e) => {
        e.preventDefault();
    };

    const confirmarCambioEstado = async () => {
        if (!confirmItem) {
            return;
        }

        const nuevoEstado =
            confirmItem.estado === "ACTIVO" ? "INACTIVO" : "ACTIVO";

        try {
            setConfirmLoading(true);

            await api.patch(`/api/admin/restaurantes/${confirmItem.id}/estado`, {
                estado: nuevoEstado,
            });

            await recargar();
            cerrarConfirmEstado();
        } catch (err) {
            setError(
                err.response?.data?.message ||
                "No se pudo actualizar el estado"
            );
        } finally {
            setConfirmLoading(false);
        }
    };

    const ImagePreview = ({ imageUrl, title }) => {
        if (!imageUrl) {
            return (
                <div className="restaurantes-page__image-placeholder">
                    Sin imagen
                </div>
            );
        }

        return (
            <img
                src={imageUrl}
                alt={title || "Restaurante"}
                className="restaurantes-page__image"
                onError={(e) => {
                    e.currentTarget.style.display = "none";
                }}
            />
        );
    };

    return (
        <div className="restaurantes-page">
            <div className="restaurantes-page__header">
                <div>
                    <h1 className="restaurantes-page__title">Restaurantes</h1>
                    <p className="restaurantes-page__subtitle">
                        Gestión de restaurantes registrados
                    </p>
                </div>

                <button
                    type="button"
                    className="restaurantes-page__top-icon-button restaurantes-page__top-icon-button--primary"
                    onClick={abrirCrear}
                    title="Nuevo restaurante"
                    aria-label="Nuevo restaurante"
                >
                    <AddRoundedIcon fontSize="small" />
                </button>
            </div>

            <div className="restaurantes-page__search-card">
                <div className="restaurantes-page__search-content">
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
                <div className="restaurantes-page__loader">
                    <CircularProgress />
                </div>
            )}

            {!loading && error && <Alert severity="error">{error}</Alert>}

            {!loading && !error && (
                <div className="restaurantes-page__list">
                    {restaurantesFiltrados.map((item) => (
                        <div className="restaurantes-page__item" key={item.id}>
                            <div className="restaurantes-page__item-content">
                                <div className="restaurantes-page__item-image">
                                    <ImagePreview
                                        imageUrl={item.imagenUrl}
                                        title={item.nombre}
                                    />
                                </div>

                                <div className="restaurantes-page__item-main">
                                    <div className="restaurantes-page__item-top">
                                        <h3 className="restaurantes-page__item-name">
                                            {item.nombre}
                                        </h3>

                                        <span className={estadoClase(item.estado)}>
                                            {item.estado}
                                        </span>
                                    </div>

                                    <p className="restaurantes-page__item-line">
                                        {item.cedulaJuridica}
                                    </p>

                                    <p className="restaurantes-page__item-line">
                                        {item.direccion}
                                    </p>

                                    <p className="restaurantes-page__item-line">
                                        {item.tipoComida}
                                    </p>

                                    {item.latitud != null &&
                                        item.longitud != null && (
                                            <p className="restaurantes-page__item-line">
                                                {item.latitud}, {item.longitud}
                                            </p>
                                        )}
                                </div>

                                <div className="restaurantes-page__item-actions">
                                    <button
                                        type="button"
                                        className="restaurantes-page__icon-button restaurantes-page__icon-button--accent"
                                        onClick={() => abrirEditar(item)}
                                        title="Editar"
                                        aria-label="Editar"
                                    >
                                        <EditRoundedIcon fontSize="small" />
                                    </button>

                                    <button
                                        type="button"
                                        className={`restaurantes-page__icon-button ${
                                            item.estado === "ACTIVO"
                                                ? "restaurantes-page__icon-button--warning"
                                                : "restaurantes-page__icon-button--success"
                                        }`}
                                        onClick={() => abrirConfirmEstado(item)}
                                        title={
                                            item.estado === "ACTIVO"
                                                ? "Inactivar"
                                                : "Activar"
                                        }
                                        aria-label={
                                            item.estado === "ACTIVO"
                                                ? "Inactivar"
                                                : "Activar"
                                        }
                                    >
                                        <PowerSettingsNewRoundedIcon fontSize="small" />
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}

                    {!restaurantesFiltrados.length && (
                        <div className="restaurantes-page__empty">
                            No hay restaurantes que coincidan con la búsqueda.
                        </div>
                    )}
                </div>
            )}

            <Dialog
                open={dialogOpen}
                onClose={cerrarDialog}
                fullWidth
                maxWidth="sm"
            >
                <form onSubmit={guardarRestaurante}>
                    <DialogTitle>
                        {editingItem ? "Editar restaurante" : "Nuevo restaurante"}
                    </DialogTitle>

                    <DialogContent dividers>
                        <div className="restaurantes-page__form">
                            <TextField
                                label="Nombre"
                                value={form.nombre}
                                onChange={(e) =>
                                    handleChange("nombre", e.target.value)
                                }
                                fullWidth
                                required
                            />

                            <TextField
                                label="Cédula jurídica"
                                value={form.cedulaJuridica}
                                onChange={(e) =>
                                    handleChange("cedulaJuridica", e.target.value)
                                }
                                fullWidth
                                required
                            />

                            <TextField
                                label="Dirección"
                                value={form.direccion}
                                onChange={(e) =>
                                    handleChange("direccion", e.target.value)
                                }
                                fullWidth
                                required
                            />

                            <LocationPickerMap
                                latitud={
                                    form.latitud === ""
                                        ? null
                                        : Number(form.latitud)
                                }
                                longitud={
                                    form.longitud === ""
                                        ? null
                                        : Number(form.longitud)
                                }
                                direccion={form.direccion}
                                onChange={({ latitud, longitud, direccion }) => {
                                    setForm((prev) => ({
                                        ...prev,
                                        latitud: latitud ?? "",
                                        longitud: longitud ?? "",
                                        direccion: direccion || prev.direccion,
                                    }));
                                }}
                            />

                            <TextField
                                label="Latitud"
                                value={form.latitud}
                                onChange={(e) =>
                                    handleChange("latitud", e.target.value)
                                }
                                fullWidth
                                required
                            />

                            <TextField
                                label="Longitud"
                                value={form.longitud}
                                onChange={(e) =>
                                    handleChange("longitud", e.target.value)
                                }
                                fullWidth
                                required
                            />

                            <TextField
                                select
                                label="Tipo de comida"
                                value={form.tipoComida}
                                onChange={(e) =>
                                    handleChange("tipoComida", e.target.value)
                                }
                                fullWidth
                                required
                            >
                                {tiposComida.map((tipo) => (
                                    <MenuItem
                                        key={tipo.value}
                                        value={tipo.value}
                                    >
                                        {tipo.label}
                                    </MenuItem>
                                ))}
                            </TextField>

                            <TextField
                                label="URL de imagen"
                                value={form.imagenUrl}
                                onChange={(e) =>
                                    handleChange("imagenUrl", e.target.value)
                                }
                                fullWidth
                                placeholder="https://..."
                            />

                            <label
                                className="app-upload-dropzone"
                                onDrop={handleDropImage}
                                onDragOver={handleDragOverImage}
                            >
                                <input
                                    type="file"
                                    accept="image/png,image/jpeg,image/webp"
                                    hidden
                                    onChange={(e) =>
                                        subirImagen(e.target.files?.[0])
                                    }
                                />

                                <span className="app-upload-dropzone__icon">
                                    ＋
                                </span>

                                <span className="app-upload-dropzone__title">
                                    Subir imagen
                                </span>

                                <span className="app-upload-dropzone__hint">
                                    Arrastrá un PNG, JPG o WEBP aquí
                                </span>
                            </label>

                            <div className="restaurantes-page__preview-card">
                                <ImagePreview
                                    imageUrl={form.imagenUrl}
                                    title={form.nombre}
                                />
                            </div>
                        </div>
                    </DialogContent>

                    <DialogActions>
                        <button
                            type="button"
                            className="restaurantes-page__dialog-icon-button restaurantes-page__dialog-icon-button--secondary"
                            onClick={cerrarDialog}
                            title="Cancelar"
                            aria-label="Cancelar"
                        >
                            <CloseRoundedIcon fontSize="small" />
                        </button>

                        <button
                            type="submit"
                            className="restaurantes-page__dialog-icon-button restaurantes-page__dialog-icon-button--primary"
                            disabled={saving}
                            title="Guardar"
                            aria-label="Guardar"
                        >
                            <SaveRoundedIcon fontSize="small" />
                        </button>
                    </DialogActions>
                </form>
            </Dialog>

            <Dialog
                open={confirmOpen}
                onClose={cerrarConfirmEstado}
                maxWidth="xs"
                fullWidth
            >
                <DialogTitle>
                    {confirmItem?.estado === "ACTIVO"
                        ? "Inactivar restaurante"
                        : "Activar restaurante"}
                </DialogTitle>

                <DialogContent dividers>
                    <p className="restaurantes-page__dialog-text">
                        {confirmItem?.estado === "ACTIVO"
                            ? `Se inactivará ${confirmItem?.nombre}.`
                            : `Se activará ${confirmItem?.nombre}.`}
                    </p>
                </DialogContent>

                <DialogActions>
                    <button
                        type="button"
                        className="restaurantes-page__dialog-icon-button restaurantes-page__dialog-icon-button--secondary"
                        onClick={cerrarConfirmEstado}
                        title="Cancelar"
                        aria-label="Cancelar"
                    >
                        <CloseRoundedIcon fontSize="small" />
                    </button>

                    <button
                        type="button"
                        className="restaurantes-page__dialog-icon-button restaurantes-page__dialog-icon-button--primary"
                        onClick={confirmarCambioEstado}
                        disabled={confirmLoading}
                        title="Confirmar"
                        aria-label="Confirmar"
                    >
                        <SaveRoundedIcon fontSize="small" />
                    </button>
                </DialogActions>
            </Dialog>
        </div>
    );
}