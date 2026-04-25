import { useEffect, useMemo, useState } from "react";
import Alert from "@mui/material/Alert";
import CircularProgress from "@mui/material/CircularProgress";
import InputAdornment from "@mui/material/InputAdornment";
import TextField from "@mui/material/TextField";
import SearchRoundedIcon from "@mui/icons-material/SearchRounded";
import PeopleRoundedIcon from "@mui/icons-material/PeopleRounded";
import StoreRoundedIcon from "@mui/icons-material/StoreRounded";
import ReceiptLongRoundedIcon from "@mui/icons-material/ReceiptLongRounded";
import ReportRoundedIcon from "@mui/icons-material/ReportRounded";
import AdminPanelSettingsRoundedIcon from "@mui/icons-material/AdminPanelSettingsRounded";
import PersonRoundedIcon from "@mui/icons-material/PersonRounded";
import TwoWheelerRoundedIcon from "@mui/icons-material/TwoWheelerRounded";
import CheckCircleRoundedIcon from "@mui/icons-material/CheckCircleRounded";
import BlockRoundedIcon from "@mui/icons-material/BlockRounded";
import RestaurantRoundedIcon from "@mui/icons-material/RestaurantRounded";
import BarChartRoundedIcon from "@mui/icons-material/BarChartRounded";
import api from "../../api/axios";
import "./DashboardPage.css";

const resumenCards = [
    { key: "usuarios", label: "Usuarios", icon: <PeopleRoundedIcon fontSize="small" /> },
    { key: "restaurantes", label: "Restaurantes", icon: <StoreRoundedIcon fontSize="small" /> },
    { key: "pedidos", label: "Pedidos", icon: <ReceiptLongRoundedIcon fontSize="small" /> },
    { key: "quejas", label: "Quejas", icon: <ReportRoundedIcon fontSize="small" /> },
];

function porcentaje(valor, maximo) {
    if (!maximo || maximo <= 0) return 0;
    return Math.max(8, Math.round((valor / maximo) * 100));
}

export default function DashboardPage() {
    const [data, setData] = useState(null);
    const [filtro, setFiltro] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let activo = true;

        const cargar = async () => {
            try {
                const response = await api.get("/api/admin/reportes/dashboard");
                if (!activo) return;
                setData(response.data);
                setError("");
            } catch (err) {
                if (!activo) return;
                setError(err.response?.data?.message || "No se pudo cargar el dashboard");
            } finally {
                if (activo) setLoading(false);
            }
        };

        cargar();

        return () => {
            activo = false;
        };
    }, []);

    const q = filtro.trim().toLowerCase();

    const pedidosPorEstado = useMemo(() => {
        const items = data?.pedidosPorEstado || [];
        if (!q) return items;
        return items.filter((item) => item.etiqueta.toLowerCase().includes(q));
    }, [data, q]);

    const topRestaurantes = useMemo(() => {
        const items = data?.topRestaurantes || [];
        if (!q) return items;
        return items.filter((item) => item.nombre.toLowerCase().includes(q));
    }, [data, q]);

    const topRepartidores = useMemo(() => {
        const items = data?.topRepartidores || [];
        if (!q) return items;
        return items.filter((item) => item.nombre.toLowerCase().includes(q));
    }, [data, q]);

    const maxEstado = Math.max(...(pedidosPorEstado.map((i) => i.total) || [0]));
    const maxRest = Math.max(...(topRestaurantes.map((i) => i.total) || [0]));
    const maxRep = Math.max(...(topRepartidores.map((i) => i.pedidosAsignados) || [0]));

    return (
        <div className="dashboard-page">
            <div className="dashboard-page__header">
                <div>
                    <h1 className="dashboard-page__title">Dashboard</h1>
                    <p className="dashboard-page__subtitle">Resumen general</p>
                </div>
            </div>

            <div className="dashboard-page__search-card">
                <div className="dashboard-page__search-content">
                    <TextField
                        fullWidth
                        placeholder="Filtrar dashboard"
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
                <div className="dashboard-page__loader">
                    <CircularProgress />
                </div>
            )}

            {!loading && error && <Alert severity="error">{error}</Alert>}

            {!loading && !error && data && (
                <>
                    <div className="dashboard-page__summary-grid">
                        {resumenCards.map((card) => (
                            <div className="dashboard-page__summary-card" key={card.key}>
                                <div className="dashboard-page__summary-icon">{card.icon}</div>
                                <div className="dashboard-page__summary-body">
                                    <p className="dashboard-page__summary-label">{card.label}</p>
                                    <h2 className="dashboard-page__summary-value">
                                        {data.resumen?.[card.key] ?? 0}
                                    </h2>
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className="dashboard-page__grid">
                        <div className="dashboard-page__section">
                            <div className="dashboard-page__section-head">
                                <PeopleRoundedIcon fontSize="small" />
                                <span>Usuarios</span>
                            </div>

                            <div className="dashboard-page__mini-grid">
                                <div className="dashboard-page__mini-card">
                                    <AdminPanelSettingsRoundedIcon fontSize="small" />
                                    <strong>{data.usuariosDetalle?.admins ?? 0}</strong>
                                    <span>Admins</span>
                                </div>

                                <div className="dashboard-page__mini-card">
                                    <PersonRoundedIcon fontSize="small" />
                                    <strong>{data.usuariosDetalle?.clientes ?? 0}</strong>
                                    <span>Clientes</span>
                                </div>

                                <div className="dashboard-page__mini-card">
                                    <TwoWheelerRoundedIcon fontSize="small" />
                                    <strong>{data.usuariosDetalle?.repartidores ?? 0}</strong>
                                    <span>Repart.</span>
                                </div>

                                <div className="dashboard-page__mini-card">
                                    <CheckCircleRoundedIcon fontSize="small" />
                                    <strong>{data.usuariosDetalle?.activos ?? 0}</strong>
                                    <span>Activos</span>
                                </div>

                                <div className="dashboard-page__mini-card">
                                    <BlockRoundedIcon fontSize="small" />
                                    <strong>{data.usuariosDetalle?.suspendidos ?? 0}</strong>
                                    <span>Suspend.</span>
                                </div>
                            </div>
                        </div>

                        <div className="dashboard-page__section">
                            <div className="dashboard-page__section-head">
                                <ReportRoundedIcon fontSize="small" />
                                <span>Quejas</span>
                            </div>

                            <div className="dashboard-page__mini-grid dashboard-page__mini-grid--four">
                                <div className="dashboard-page__mini-card">
                                    <strong>{data.quejasDetalle?.pendientes ?? 0}</strong>
                                    <span>Pend.</span>
                                </div>
                                <div className="dashboard-page__mini-card">
                                    <strong>{data.quejasDetalle?.enRevision ?? 0}</strong>
                                    <span>Rev.</span>
                                </div>
                                <div className="dashboard-page__mini-card">
                                    <strong>{data.quejasDetalle?.resueltas ?? 0}</strong>
                                    <span>Res.</span>
                                </div>
                                <div className="dashboard-page__mini-card">
                                    <strong>{data.quejasDetalle?.rechazadas ?? 0}</strong>
                                    <span>Rech.</span>
                                </div>
                            </div>
                        </div>

                        <div className="dashboard-page__section">
                            <div className="dashboard-page__section-head">
                                <BarChartRoundedIcon fontSize="small" />
                                <span>Pedidos</span>
                            </div>

                            <div className="dashboard-page__list">
                                {pedidosPorEstado.length ? (
                                    pedidosPorEstado.map((item) => (
                                        <div className="dashboard-page__metric-row" key={item.etiqueta}>
                                            <div className="dashboard-page__metric-top">
                                                <span>{item.etiqueta}</span>
                                                <strong>{item.total}</strong>
                                            </div>
                                            <div className="dashboard-page__bar">
                                                <div
                                                    className="dashboard-page__bar-fill dashboard-page__bar-fill--green"
                                                    style={{ width: `${porcentaje(item.total, maxEstado)}%` }}
                                                />
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className="dashboard-page__empty">Sin resultados</div>
                                )}
                            </div>
                        </div>

                        <div className="dashboard-page__section">
                            <div className="dashboard-page__section-head">
                                <RestaurantRoundedIcon fontSize="small" />
                                <span>Restaurantes</span>
                            </div>

                            <div className="dashboard-page__list">
                                {topRestaurantes.length ? (
                                    topRestaurantes.map((item) => (
                                        <div className="dashboard-page__metric-row" key={item.nombre}>
                                            <div className="dashboard-page__metric-top">
                                                <span>{item.nombre}</span>
                                                <strong>{item.total}</strong>
                                            </div>
                                            <div className="dashboard-page__bar">
                                                <div
                                                    className="dashboard-page__bar-fill dashboard-page__bar-fill--mint"
                                                    style={{ width: `${porcentaje(item.total, maxRest)}%` }}
                                                />
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className="dashboard-page__empty">Sin resultados</div>
                                )}
                            </div>
                        </div>

                        <div className="dashboard-page__section dashboard-page__section--full">
                            <div className="dashboard-page__section-head">
                                <TwoWheelerRoundedIcon fontSize="small" />
                                <span>Repartidores</span>
                            </div>

                            <div className="dashboard-page__list">
                                {topRepartidores.length ? (
                                    topRepartidores.map((item) => (
                                        <div className="dashboard-page__metric-row" key={item.nombre}>
                                            <div className="dashboard-page__metric-top">
                                                <span>{item.nombre}</span>
                                                <strong>{item.pedidosAsignados}</strong>
                                            </div>
                                            <div className="dashboard-page__bar">
                                                <div
                                                    className="dashboard-page__bar-fill dashboard-page__bar-fill--dark"
                                                    style={{ width: `${porcentaje(item.pedidosAsignados, maxRep)}%` }}
                                                />
                                            </div>
                                            <p className="dashboard-page__helper">
                                                {item.amonestacionesActivas} amon.
                                            </p>
                                        </div>
                                    ))
                                ) : (
                                    <div className="dashboard-page__empty">Sin resultados</div>
                                )}
                            </div>
                        </div>
                    </div>
                </>
            )}
        </div>
    );
}