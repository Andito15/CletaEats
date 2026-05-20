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
import "./CombosPage.css";

const initialForm = {
    restauranteId: "",
    numeroCombo: "",
    nombre: "",
    descripcion: "",
    precio: "",
    imagenUrl: "",
};

function estadoClase(estado) {
    return estado === "ACTIVO"
        ? "app-chip app-chip--success"
        : "app-chip app-chip--neutral";
}

export default function CombosPage() {
    const [restaurantes, setRestaurantes] = useState([]);
    const [selectedRestauranteId, setSelectedRestauranteId] = useState("");
    const [combos, setCombos] = useState([]);
    const [filtro, setFiltro] = useState("");
    const [loading, setLoading] = useState(true);
    const [loadingCombos, setLoadingCombos] = useState(false);
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
                if (!activo) return;

                const data = response.data || [];
                setRestaurantes(data);

                if (data.length > 0) {
                    const primerId = String(data[0].id);
                    setSelectedRestauranteId(primerId);
                }

                setError("");
            } catch (err) {
                if (!activo) return;
                setError(err.response?.data?.message || "No se pudieron cargar los restaurantes");
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

    useEffect(() => {
        if (!selectedRestauranteId) {
            setCombos([]);
            return;
        }

        let activo = true;

        const cargarCombos = async () => {
            try {
                setLoadingCombos(true);
                const response = await api.get(
                    `/api/restaurantes/${selectedRestauranteId}/combos?soloActivos=false`
                );
                if (!activo) return;
                setCombos(response.data || []);
                setError("");
            } catch (err) {
                if (!activo) return;
                setError(err.response?.data?.message || "No se pudieron cargar los combos");
            } finally {
                if (activo) {
                    setLoadingCombos(false);
                }
            }
        };

        cargarCombos();

        return () => {
            activo = false;
        };
    }, [selectedRestauranteId]);

    const recargarCombos = async (restauranteId = selectedRestauranteId) => {
        if (!restauranteId) return;

        try {
            const response = await api.get(
                `/api/restaurantes/${restauranteId}/combos?soloActivos=false`
            );
            setCombos(response.data || []);
            setError("");
        } catch (err) {
            setError(err.response?.data?.message || "No se pudieron cargar los combos");
        }
    };

    const combosFiltrados = useMemo(() => {
        const q = filtro.trim().toLowerCase();
        if (!q) return combos;

        return combos.filter((combo) => {
            return (
                String(combo.numeroCombo).includes(q) ||
                combo.nombre.toLowerCase().includes(q) ||
                combo.descripcion.toLowerCase().includes(q) ||
                String(combo.precio).toLowerCase().includes(q) ||
                combo.estado.toLowerCase().includes(q)
            );
        });
    }, [combos, filtro]);

    const abrirCrear = () => {
        setEditingItem(null);
        setForm({
            ...initialForm,
            restauranteId: selectedRestauranteId || "",
        });
        setDialogOpen(true);
    };

    const abrirEditar = (item) => {
        setEditingItem(item);
        setForm({
            restauranteId: String(item.restauranteId || selectedRestauranteId || ""),
            numeroCombo: String(item.numeroCombo || ""),
            nombre: item.nombre || "",
            descripcion: item.descripcion || "",
            precio: item.precio != null ? String(item.precio) : "",
            imagenUrl: item.imagenUrl || "",
        });
        setDialogOpen(true);
    };

    const cerrarDialog = () => {
        if (saving) return;
        setDialogOpen(false);
        setEditingItem(null);
        setForm(initialForm);
    };

    const abrirConfirmEstado = (item) => {
        setConfirmItem(item);
        setConfirmOpen(true);
    };

    const cerrarConfirmEstado = () => {
        if (confirmLoading) return;
        setConfirmOpen(false);
        setConfirmItem(null);
    };

    const handleChange = (field, value) => {
        setForm((prev) => ({
            ...prev,
            [field]: value,
        }));
    };

    const guardarCombo = async (e) => {
        e.preventDefault();
        setSaving(true);
        setError("");

        const restauranteId = editingItem
            ? editingItem.restauranteId
            : Number(form.restauranteId);

        const payload = {
            numeroCombo: Number(form.numeroCombo),
            nombre: form.nombre.trim(),
            descripcion: form.descripcion.trim(),
            precio: Number(form.precio),
            imagenUrl: form.imagenUrl.trim() || null,
        };

        try {
            if (editingItem) {
                await api.put(`/api/admin/combos/${editingItem.id}`, payload);
            } else {
                await api.post(`/api/admin/restaurantes/${restauranteId}/combos`, payload);
            }

            if (!editingItem && restauranteId) {
                setSelectedRestauranteId(String(restauranteId));
            }

            await recargarCombos(String(restauranteId));
            cerrarDialog();
        } catch (err) {
            setError(err.response?.data?.message || "No se pudo guardar el combo");
        } finally {
            setSaving(false);
        }
    };

    const confirmarCambioEstado = async () => {
        if (!confirmItem) return;

        const nuevoEstado = confirmItem.estado === "ACTIVO" ? "INACTIVO" : "ACTIVO";

        try {
            setConfirmLoading(true);
            await api.patch(`/api/admin/combos/${confirmItem.id}/estado`, {
                estado: nuevoEstado,
            });
            await recargarCombos();
            cerrarConfirmEstado();
        } catch (err) {
            setError(err.response?.data?.message || "No se pudo actualizar el estado");
        } finally {
            setConfirmLoading(false);
        }
    };

    const ImagePreview = ({ imageUrl, title }) => {
        if (!imageUrl) {
            return (
                <div className="combos-page__image-placeholder">
                    Sin imagen
                </div>
            );
        }

        return (
            <img
                src={imageUrl}
                alt={title || "Combo"}
                className="combos-page__image"
                onError={(e) => {
                    e.currentTarget.style.display = "none";
                }}
            />
        );
    };

    return (
        <div className="combos-page">
            <div className="combos-page__header">
                <div>
                    <h1 className="combos-page__title">Combos</h1>
                    <p className="combos-page__subtitle">Gestión por restaurante</p>
                </div>

                <button
                    type="button"
                    className="combos-page__top-icon-button combos-page__top-icon-button--primary"
                    onClick={abrirCrear}
                    title="Nuevo combo"
                    aria-label="Nuevo combo"
                    disabled={!selectedRestauranteId}
                >
                    <AddRoundedIcon fontSize="small" />
                </button>
            </div>

            <div className="combos-page__toolbar">
                <div className="combos-page__toolbar-card">
                    <TextField
                        select
                        fullWidth
                        label="Restaurante"
                        value={selectedRestauranteId}
                        onChange={(e) => setSelectedRestauranteId(e.target.value)}
                    >
                        {restaurantes.map((item) => (
                            <MenuItem key={item.id} value={String(item.id)}>
                                {item.nombre}
                            </MenuItem>
                        ))}
                    </TextField>
                </div>

                <div className="combos-page__toolbar-card">
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
                <div className="combos-page__loader">
                    <CircularProgress />
                </div>
            )}

            {!loading && error && <Alert severity="error">{error}</Alert>}

            {!loading && !error && loadingCombos && (
                <div className="combos-page__loader">
                    <CircularProgress />
                </div>
            )}

            {!loading && !error && !loadingCombos && (
                <div className="combos-page__list">
                    {combosFiltrados.map((item) => (
                        <div className="combos-page__item" key={item.id}>
                            <div className="combos-page__item-content">
                                <div className="combos-page__item-image">
                                    <ImagePreview
                                        imageUrl={item.imagenUrl}
                                        title={item.nombre}
                                    />
                                </div>
                                <div className="combos-page__item-main">
                                    <div className="combos-page__item-top">
                                        <h3 className="combos-page__item-name">
                                            #{item.numeroCombo} · {item.nombre}
                                        </h3>
                                        <span className={estadoClase(item.estado)}>{item.estado}</span>
                                    </div>

                                    <p className="combos-page__item-line">{item.descripcion}</p>
                                    <p className="combos-page__item-line">
                                        ₡ {Number(item.precio || 0).toLocaleString("es-CR")}
                                    </p>
                                </div>

                                <div className="combos-page__item-actions">
                                    <button
                                        type="button"
                                        className="combos-page__icon-button combos-page__icon-button--accent"
                                        onClick={() => abrirEditar(item)}
                                        title="Editar"
                                        aria-label="Editar"
                                    >
                                        <EditRoundedIcon fontSize="small" />
                                    </button>

                                    <button
                                        type="button"
                                        className={`combos-page__icon-button ${
                                            item.estado === "ACTIVO"
                                                ? "combos-page__icon-button--warning"
                                                : "combos-page__icon-button--success"
                                        }`}
                                        onClick={() => abrirConfirmEstado(item)}
                                        title={item.estado === "ACTIVO" ? "Inactivar" : "Activar"}
                                        aria-label={item.estado === "ACTIVO" ? "Inactivar" : "Activar"}
                                    >
                                        <PowerSettingsNewRoundedIcon fontSize="small" />
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}

                    {!combosFiltrados.length && (
                        <div className="combos-page__empty">Sin combos</div>
                    )}
                </div>
            )}

            <Dialog open={dialogOpen} onClose={cerrarDialog} fullWidth maxWidth="sm">
                <form onSubmit={guardarCombo}>
                    <DialogTitle>{editingItem ? "Editar combo" : "Nuevo combo"}</DialogTitle>

                    <DialogContent dividers>
                        <div className="combos-page__form">
                            <TextField
                                select
                                label="Restaurante"
                                value={form.restauranteId}
                                onChange={(e) => handleChange("restauranteId", e.target.value)}
                                fullWidth
                                disabled={Boolean(editingItem)}
                                required
                            >
                                {restaurantes.map((item) => (
                                    <MenuItem key={item.id} value={String(item.id)}>
                                        {item.nombre}
                                    </MenuItem>
                                ))}
                            </TextField>

                            <TextField
                                label="Número"
                                type="number"
                                value={form.numeroCombo}
                                onChange={(e) => handleChange("numeroCombo", e.target.value)}
                                fullWidth
                                inputProps={{ min: 1, max: 9 }}
                                required
                            />

                            <TextField
                                label="Nombre"
                                value={form.nombre}
                                onChange={(e) => handleChange("nombre", e.target.value)}
                                fullWidth
                                required
                            />

                            <TextField
                                label="Descripción"
                                value={form.descripcion}
                                onChange={(e) => handleChange("descripcion", e.target.value)}
                                fullWidth
                                multiline
                                minRows={3}
                                required
                            />

                            <TextField
                                label="Precio"
                                type="number"
                                value={form.precio}
                                onChange={(e) => handleChange("precio", e.target.value)}
                                fullWidth
                                inputProps={{ min: 0.01, step: 0.01 }}
                                required
                            />

                            <TextField
                                label="URL de imagen"
                                value={form.imagenUrl}
                                onChange={(e) => handleChange("imagenUrl", e.target.value)}
                                fullWidth
                                placeholder="https://..."
                            />

                            <div className="combos-page__preview-card">
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
                            className="combos-page__dialog-icon-button combos-page__dialog-icon-button--secondary"
                            onClick={cerrarDialog}
                            title="Cancelar"
                            aria-label="Cancelar"
                        >
                            <CloseRoundedIcon fontSize="small" />
                        </button>

                        <button
                            type="submit"
                            className="combos-page__dialog-icon-button combos-page__dialog-icon-button--primary"
                            disabled={saving}
                            title="Guardar"
                            aria-label="Guardar"
                        >
                            <SaveRoundedIcon fontSize="small" />
                        </button>
                    </DialogActions>
                </form>
            </Dialog>

            <Dialog open={confirmOpen} onClose={cerrarConfirmEstado} maxWidth="xs" fullWidth>
                <DialogTitle>
                    {confirmItem?.estado === "ACTIVO" ? "Inactivar combo" : "Activar combo"}
                </DialogTitle>

                <DialogContent dividers>
                    <p className="combos-page__dialog-text">
                        {confirmItem?.estado === "ACTIVO"
                            ? `Se inactivará ${confirmItem?.nombre}.`
                            : `Se activará ${confirmItem?.nombre}.`}
                    </p>
                </DialogContent>

                <DialogActions>
                    <button
                        type="button"
                        className="combos-page__dialog-icon-button combos-page__dialog-icon-button--secondary"
                        onClick={cerrarConfirmEstado}
                        title="Cancelar"
                        aria-label="Cancelar"
                    >
                        <CloseRoundedIcon fontSize="small" />
                    </button>

                    <button
                        type="button"
                        className="combos-page__dialog-icon-button combos-page__dialog-icon-button--primary"
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