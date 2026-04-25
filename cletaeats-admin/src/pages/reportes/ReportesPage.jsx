import { useEffect, useState } from "react";
import Alert from "@mui/material/Alert";
import CircularProgress from "@mui/material/CircularProgress";
import PeopleRoundedIcon from "@mui/icons-material/PeopleRounded";
import StoreRoundedIcon from "@mui/icons-material/StoreRounded";
import FastfoodRoundedIcon from "@mui/icons-material/FastfoodRounded";
import ReceiptLongRoundedIcon from "@mui/icons-material/ReceiptLongRounded";
import RestaurantRoundedIcon from "@mui/icons-material/RestaurantRounded";
import PersonRoundedIcon from "@mui/icons-material/PersonRounded";
import TwoWheelerRoundedIcon from "@mui/icons-material/TwoWheelerRounded";
import BarChartRoundedIcon from "@mui/icons-material/BarChartRounded";
import api from "../../api/axios";
import "./ReportesPage.css";

const resumenCards = [
    { key: "usuarios", icon: <PeopleRoundedIcon fontSize="small" /> },
    { key: "restaurantes", icon: <StoreRoundedIcon fontSize="small" /> },
    { key: "combosActivos", icon: <FastfoodRoundedIcon fontSize="small" /> },
    { key: "pedidos", icon: <ReceiptLongRoundedIcon fontSize="small" /> },
];

const resumenLabels = {
    usuarios: "Usuarios",
    restaurantes: "Restaurantes",
    combosActivos: "Combos",
    pedidos: "Pedidos",
};

function porcentaje(valor, maximo) {
    if (!maximo || maximo <= 0) return 0;
    return Math.max(8, Math.round((valor / maximo) * 100));
}

export default function ReportesPage() {
    const [data, setData] = useState(null);
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
                setError(err.response?.data?.message || "No se pudieron cargar los reportes");
            } finally {
                if (activo) setLoading(false);
            }
        };

        cargar();

        return () => {
            activo = false;
        };
    }, []);

    const maxEstado = Math.max(...(data?.pedidosPorEstado?.map((i) => i.total) || [0]));
    const maxRest = Math.max(...(data?.topRestaurantes?.map((i) => i.total) || [0]));
    const maxCli = Math.max(...(data?.topClientes?.map((i) => i.total) || [0]));
    const maxRep = Math.max(...(data?.topRepartidores?.map((i) => i.pedidosAsignados) || [0]));

    return (
        <div className="reportes-page">
            <div className="reportes-page__header">
                <div>
                    <h1 className="reportes-page__title">Reportes</h1>
                    <p className="reportes-page__subtitle">Resumen general</p>
                </div>
            </div>

            {loading && (
                <div className="reportes-page__loader">
                    <CircularProgress />
                </div>
            )}

            {!loading && error && <Alert severity="error">{error}</Alert>}

            {!loading && !error && data && (
                <>
                    <div className="reportes-page__summary-grid">
                        {resumenCards.map((card) => (
                            <div className="reportes-page__summary-card" key={card.key}>
                                <div className="reportes-page__summary-icon">{card.icon}</div>
                                <div className="reportes-page__summary-body">
                                    <p className="reportes-page__summary-label">
                                        {resumenLabels[card.key]}
                                    </p>
                                    <h2 className="reportes-page__summary-value">
                                        {data.resumen?.[card.key] ?? 0}
                                    </h2>
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className="reportes-page__grid">
                        <div className="reportes-page__section">
                            <div className="reportes-page__section-head">
                                <BarChartRoundedIcon fontSize="small" />
                                <span>Estados</span>
                            </div>

                            <div className="reportes-page__list">
                                {data.pedidosPorEstado?.length ? (
                                    data.pedidosPorEstado.map((item) => (
                                        <div className="reportes-page__metric-row" key={item.etiqueta}>
                                            <div className="reportes-page__metric-top">
                                                <span>{item.etiqueta}</span>
                                                <strong>{item.total}</strong>
                                            </div>
                                            <div className="reportes-page__bar">
                                                <div
                                                    className="reportes-page__bar-fill reportes-page__bar-fill--green"
                                                    style={{ width: `${porcentaje(item.total, maxEstado)}%` }}
                                                />
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className="reportes-page__empty">Sin datos</div>
                                )}
                            </div>
                        </div>

                        <div className="reportes-page__section">
                            <div className="reportes-page__section-head">
                                <RestaurantRoundedIcon fontSize="small" />
                                <span>Restaurantes</span>
                            </div>

                            <div className="reportes-page__list">
                                {data.topRestaurantes?.length ? (
                                    data.topRestaurantes.map((item) => (
                                        <div className="reportes-page__metric-row" key={item.nombre}>
                                            <div className="reportes-page__metric-top">
                                                <span>{item.nombre}</span>
                                                <strong>{item.total}</strong>
                                            </div>
                                            <div className="reportes-page__bar">
                                                <div
                                                    className="reportes-page__bar-fill reportes-page__bar-fill--blue"
                                                    style={{ width: `${porcentaje(item.total, maxRest)}%` }}
                                                />
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className="reportes-page__empty">Sin datos</div>
                                )}
                            </div>
                        </div>

                        <div className="reportes-page__section">
                            <div className="reportes-page__section-head">
                                <PersonRoundedIcon fontSize="small" />
                                <span>Clientes</span>
                            </div>

                            <div className="reportes-page__list">
                                {data.topClientes?.length ? (
                                    data.topClientes.map((item) => (
                                        <div className="reportes-page__metric-row" key={item.nombre}>
                                            <div className="reportes-page__metric-top">
                                                <span>{item.nombre}</span>
                                                <strong>{item.total}</strong>
                                            </div>
                                            <div className="reportes-page__bar">
                                                <div
                                                    className="reportes-page__bar-fill reportes-page__bar-fill--purple"
                                                    style={{ width: `${porcentaje(item.total, maxCli)}%` }}
                                                />
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className="reportes-page__empty">Sin datos</div>
                                )}
                            </div>
                        </div>

                        <div className="reportes-page__section">
                            <div className="reportes-page__section-head">
                                <TwoWheelerRoundedIcon fontSize="small" />
                                <span>Repartidores</span>
                            </div>

                            <div className="reportes-page__list">
                                {data.topRepartidores?.length ? (
                                    data.topRepartidores.map((item) => (
                                        <div className="reportes-page__metric-row" key={item.nombre}>
                                            <div className="reportes-page__metric-top">
                                                <span>{item.nombre}</span>
                                                <strong>{item.pedidosAsignados}</strong>
                                            </div>
                                            <div className="reportes-page__bar">
                                                <div
                                                    className="reportes-page__bar-fill reportes-page__bar-fill--orange"
                                                    style={{ width: `${porcentaje(item.pedidosAsignados, maxRep)}%` }}
                                                />
                                            </div>
                                            <p className="reportes-page__helper">
                                                {item.amonestacionesActivas} amon.
                                            </p>
                                        </div>
                                    ))
                                ) : (
                                    <div className="reportes-page__empty">Sin datos</div>
                                )}
                            </div>
                        </div>
                    </div>
                </>
            )}
        </div>
    );
}