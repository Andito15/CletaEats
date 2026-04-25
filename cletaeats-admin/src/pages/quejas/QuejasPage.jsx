import { useEffect, useMemo, useState } from "react";
import Alert from "@mui/material/Alert";
import CircularProgress from "@mui/material/CircularProgress";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import InputAdornment from "@mui/material/InputAdornment";
import TextField from "@mui/material/TextField";
import SearchRoundedIcon from "@mui/icons-material/SearchRounded";
import GavelRoundedIcon from "@mui/icons-material/GavelRounded";
import VisibilityRoundedIcon from "@mui/icons-material/VisibilityRounded";
import CloseRoundedIcon from "@mui/icons-material/CloseRounded";
import SaveRoundedIcon from "@mui/icons-material/SaveRounded";
import api from "../../api/axios";
import "./QuejasPage.css";

function estadoClase(estado) {
    switch (estado) {
        case "RESUELTA":
            return "app-chip app-chip--success";
        case "PENDIENTE":
            return "app-chip app-chip--warning";
        default:
            return "app-chip app-chip--neutral";
    }
}

function activaClase(activa) {
    return activa === "S"
        ? "app-chip app-chip--success"
        : "app-chip app-chip--neutral";
}

function formatoFecha(valor) {
    if (!valor) return "—";
    return new Date(valor).toLocaleString("es-CR");
}

export default function QuejasPage() {
    const [quejas, setQuejas] = useState([]);
    const [filtro, setFiltro] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [dialogOpen, setDialogOpen] = useState(false);
    const [selectedQueja, setSelectedQueja] = useState(null);
    const [motivo, setMotivo] = useState("");
    const [saving, setSaving] = useState(false);

    const [historialOpen, setHistorialOpen] = useState(false);
    const [historialLoading, setHistorialLoading] = useState(false);
    const [historialTitulo, setHistorialTitulo] = useState("");
    const [amonestaciones, setAmonestaciones] = useState([]);

    useEffect(() => {
        let activo = true;

        const cargarQuejas = async () => {
            try {
                const response = await api.get("/api/admin/quejas");
                if (!activo) return;
                setQuejas(response.data || []);
                setError("");
            } catch (err) {
                if (!activo) return;
                setError(err.response?.data?.message || "No se pudieron cargar las quejas");
            } finally {
                if (activo) {
                    setLoading(false);
                }
            }
        };

        cargarQuejas();

        return () => {
            activo = false;
        };
    }, []);

    const recargar = async () => {
        try {
            const response = await api.get("/api/admin/quejas");
            setQuejas(response.data || []);
            setError("");
        } catch (err) {
            setError(err.response?.data?.message || "No se pudieron cargar las quejas");
        }
    };

    const quejasFiltradas = useMemo(() => {
        const q = filtro.trim().toLowerCase();
        if (!q) return quejas;

        return quejas.filter((item) => {
            return (
                String(item.quejaId).includes(q) ||
                String(item.categoria || "").toLowerCase().includes(q) ||
                String(item.descripcion || "").toLowerCase().includes(q) ||
                String(item.estado || "").toLowerCase().includes(q) ||
                String(item.repartidorNombre || "").toLowerCase().includes(q) ||
                String(item.clienteNombre || "").toLowerCase().includes(q)
            );
        });
    }, [quejas, filtro]);

    const abrirAmonestacion = (queja) => {
        if (document.activeElement instanceof HTMLElement) {
            document.activeElement.blur();
        }

        setSelectedQueja(queja);
        setMotivo("");
        setDialogOpen(true);
    };

    const cerrarDialog = () => {
        if (saving) return;
        setDialogOpen(false);
        setSelectedQueja(null);
        setMotivo("");
    };

    const guardarAmonestacion = async (e) => {
        e.preventDefault();
        if (!selectedQueja) return;

        try {
            setSaving(true);
            await api.post(`/api/admin/quejas/${selectedQueja.quejaId}/amonestacion`, {
                motivo: motivo.trim(),
            });
            await recargar();
            cerrarDialog();
        } catch (err) {
            const backendMessage =
                err.response?.data?.message ||
                err.response?.data?.error ||
                err.response?.data?.detail ||
                "No se pudo crear la amonestación";

            setError(backendMessage);
        } finally {
            setSaving(false);
        }
    };

    const abrirHistorial = async (queja) => {
        try {
            setHistorialLoading(true);
            setHistorialTitulo(queja.repartidorNombre || "Amonestaciones");
            setHistorialOpen(true);

            const response = await api.get(`/api/admin/quejas/${queja.quejaId}/amonestaciones`);
            setAmonestaciones(response.data || []);
            setError("");
        } catch (err) {
            setError(err.response?.data?.message || "No se pudieron cargar las amonestaciones");
            setAmonestaciones([]);
        } finally {
            setHistorialLoading(false);
        }
    };

    const cerrarHistorial = () => {
        setHistorialOpen(false);
        setHistorialTitulo("");
        setAmonestaciones([]);
    };

    return (
        <div className="quejas-page">
            <div className="quejas-page__header">
                <div>
                    <h1 className="quejas-page__title">Quejas</h1>
                    <p className="quejas-page__subtitle">Gestión de incidencias</p>
                </div>
            </div>

            <div className="quejas-page__search-card">
                <div className="quejas-page__search-content">
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
                <div className="quejas-page__loader">
                    <CircularProgress />
                </div>
            )}

            {!loading && error && <Alert severity="error">{error}</Alert>}

            {!loading && !error && (
                <div className="quejas-page__list">
                    {quejasFiltradas.map((item) => (
                        <div className="quejas-page__item" key={item.quejaId}>
                            <div className="quejas-page__item-content">
                                <div className="quejas-page__item-main">
                                    <div className="quejas-page__item-top">
                                        <h3 className="quejas-page__item-name">
                                            #{item.quejaId} · {item.categoria}
                                        </h3>
                                        <span className={estadoClase(item.estado)}>{item.estado}</span>
                                    </div>

                                    <p className="quejas-page__item-line">{item.descripcion}</p>
                                    <p className="quejas-page__item-line">
                                        {item.clienteNombre} · {item.repartidorNombre}
                                    </p>
                                </div>

                                <div className="quejas-page__item-actions">
                                    <button
                                        type="button"
                                        className="quejas-page__icon-button quejas-page__icon-button--accent"
                                        onClick={() => abrirHistorial(item)}
                                        title="Ver amonestaciones"
                                        aria-label="Ver amonestaciones"
                                    >
                                        <VisibilityRoundedIcon fontSize="small" />
                                    </button>

                                    <button
                                        type="button"
                                        className="quejas-page__icon-button quejas-page__icon-button--danger"
                                        onClick={() => abrirAmonestacion(item)}
                                        title="Amonestar"
                                        aria-label="Amonestar"
                                        disabled={item.estado === "RESUELTA"}
                                    >
                                        <GavelRoundedIcon fontSize="small" />
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}

                    {!quejasFiltradas.length && (
                        <div className="quejas-page__empty">Sin quejas</div>
                    )}
                </div>
            )}

            <Dialog open={dialogOpen} onClose={cerrarDialog} fullWidth maxWidth="sm">
                <form onSubmit={guardarAmonestacion}>
                    <DialogTitle>Amonestación</DialogTitle>

                    <DialogContent dividers>
                        <div className="quejas-page__form">
                            <TextField
                                label="Motivo"
                                value={motivo}
                                onChange={(e) => setMotivo(e.target.value)}
                                fullWidth
                                multiline
                                minRows={4}
                                required
                                autoFocus
                            />
                        </div>
                    </DialogContent>

                    <DialogActions>
                        <button
                            type="button"
                            className="quejas-page__dialog-icon-button quejas-page__dialog-icon-button--secondary"
                            onClick={cerrarDialog}
                            title="Cancelar"
                            aria-label="Cancelar"
                        >
                            <CloseRoundedIcon fontSize="small" />
                        </button>

                        <button
                            type="submit"
                            className="quejas-page__dialog-icon-button quejas-page__dialog-icon-button--primary"
                            disabled={saving}
                            title="Guardar"
                            aria-label="Guardar"
                        >
                            <SaveRoundedIcon fontSize="small" />
                        </button>
                    </DialogActions>
                </form>
            </Dialog>

            <Dialog open={historialOpen} onClose={cerrarHistorial} fullWidth maxWidth="sm">
                <DialogTitle>Amonestaciones</DialogTitle>

                <DialogContent dividers>
                    <div className="quejas-page__history">
                        {historialTitulo && (
                            <p className="quejas-page__history-title">{historialTitulo}</p>
                        )}

                        {historialLoading && (
                            <div className="quejas-page__loader">
                                <CircularProgress />
                            </div>
                        )}

                        {!historialLoading && !!amonestaciones.length && (
                            <div className="quejas-page__history-list">
                                {amonestaciones.map((item) => (
                                    <div className="quejas-page__history-card" key={item.amonestacionId}>
                                        <div className="quejas-page__history-top">
                      <span className={activaClase(item.activa)}>
                        {item.activa === "S" ? "ACTIVA" : "INACTIVA"}
                      </span>
                                            <span className="quejas-page__history-date">
                        {formatoFecha(item.fechaAmonestacion)}
                      </span>
                                        </div>

                                        <p className="quejas-page__history-text">{item.motivo}</p>

                                        <p className="quejas-page__history-admin">
                                            {item.adminNombre || "—"}
                                        </p>
                                    </div>
                                ))}
                            </div>
                        )}

                        {!historialLoading && !amonestaciones.length && (
                            <div className="quejas-page__empty">Sin amonestaciones</div>
                        )}
                    </div>
                </DialogContent>

                <DialogActions>
                    <button
                        type="button"
                        className="quejas-page__dialog-icon-button quejas-page__dialog-icon-button--secondary"
                        onClick={cerrarHistorial}
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