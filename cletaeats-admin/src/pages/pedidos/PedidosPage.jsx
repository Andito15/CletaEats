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
import VisibilityRoundedIcon from "@mui/icons-material/VisibilityRounded";
import ReceiptLongRoundedIcon from "@mui/icons-material/ReceiptLongRounded";
import CloseRoundedIcon from "@mui/icons-material/CloseRounded";
import api from "../../api/axios";
import "./PedidosPage.css";

function estadoClase(estado) {
    switch (estado) {
        case "ENTREGADO":
            return "app-chip app-chip--success";
        case "EN_CAMINO":
            return "app-chip app-chip--warning";
        case "EN_PREPARACION":
            return "app-chip app-chip--neutral";
        default:
            return "app-chip app-chip--neutral";
    }
}

export default function PedidosPage() {
    const [pedidos, setPedidos] = useState([]);
    const [filtro, setFiltro] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [detailOpen, setDetailOpen] = useState(false);
    const [selectedPedido, setSelectedPedido] = useState(null);

    useEffect(() => {
        let activo = true;

        const cargarPedidos = async () => {
            try {
                const response = await api.get("/api/admin/pedidos");
                if (!activo) return;
                setPedidos(response.data || []);
                setError("");
            } catch (err) {
                if (!activo) return;
                setError(err.response?.data?.message || "No se pudieron cargar los pedidos");
            } finally {
                if (activo) {
                    setLoading(false);
                }
            }
        };

        cargarPedidos();

        return () => {
            activo = false;
        };
    }, []);

    const pedidosFiltrados = useMemo(() => {
        const q = filtro.trim().toLowerCase();
        if (!q) return pedidos;

        return pedidos.filter((pedido) => {
            return (
                String(pedido.pedidoId).includes(q) ||
                String(pedido.numeroPedido || "").toLowerCase().includes(q) ||
                String(pedido.estado || "").toLowerCase().includes(q) ||
                String(pedido.clienteNombre || "").toLowerCase().includes(q) ||
                String(pedido.restauranteNombre || "").toLowerCase().includes(q) ||
                String(pedido.repartidorNombre || "").toLowerCase().includes(q) ||
                String(pedido.direccionEntrega || "").toLowerCase().includes(q)
            );
        });
    }, [pedidos, filtro]);

    const abrirDetalle = async (pedidoId) => {
        try {
            const response = await api.get(`/api/admin/pedidos/${pedidoId}`);
            setSelectedPedido(response.data);
            setDetailOpen(true);
            setError("");
        } catch (err) {
            setError(err.response?.data?.message || "No se pudo cargar el detalle del pedido");
        }
    };

    const cerrarDetalle = () => {
        setDetailOpen(false);
        setSelectedPedido(null);
    };

    const formatoMoneda = (valor) => {
        return Number(valor || 0).toLocaleString("es-CR", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
        });
    };

    return (
        <div className="pedidos-page">
            <div className="pedidos-page__header">
                <div>
                    <h1 className="pedidos-page__title">Pedidos</h1>
                    <p className="pedidos-page__subtitle">Vista general del sistema</p>
                </div>
            </div>

            <div className="pedidos-page__search-card">
                <div className="pedidos-page__search-content">
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
                <div className="pedidos-page__loader">
                    <CircularProgress />
                </div>
            )}

            {!loading && error && <Alert severity="error">{error}</Alert>}

            {!loading && !error && (
                <div className="pedidos-page__list">
                    {pedidosFiltrados.map((pedido) => (
                        <div className="pedidos-page__item" key={pedido.pedidoId}>
                            <div className="pedidos-page__item-content">
                                <div className="pedidos-page__item-main">
                                    <div className="pedidos-page__item-top">
                                        <h3 className="pedidos-page__item-name">
                                            {pedido.numeroPedido}
                                        </h3>
                                        <span className={estadoClase(pedido.estado)}>
                      {pedido.estado}
                    </span>
                                    </div>

                                    <p className="pedidos-page__item-line">
                                        {pedido.clienteNombre} · {pedido.restauranteNombre}
                                    </p>
                                    <p className="pedidos-page__item-line">
                                        {pedido.repartidorNombre || "Sin repartidor"} · {pedido.direccionEntrega}
                                    </p>
                                    <p className="pedidos-page__item-line">
                                        {pedido.factura
                                            ? `₡ ${formatoMoneda(pedido.factura.montoTotal)}`
                                            : "Sin factura"}
                                    </p>
                                </div>

                                <div className="pedidos-page__item-actions">
                                    <button
                                        type="button"
                                        className="pedidos-page__icon-button pedidos-page__icon-button--accent"
                                        onClick={() => abrirDetalle(pedido.pedidoId)}
                                        title="Ver detalle"
                                        aria-label="Ver detalle"
                                    >
                                        <ReceiptLongRoundedIcon fontSize="small" />
                                    </button>

                                    {/*<button*/}
                                    {/*    type="button"*/}
                                    {/*    className="pedidos-page__icon-button pedidos-page__icon-button--info"*/}
                                    {/*    onClick={() => abrirDetalle(pedido.pedidoId)}*/}
                                    {/*    title="Ver factura"*/}
                                    {/*    aria-label="Ver factura"*/}
                                    {/*>*/}
                                    {/*    <ReceiptLongRoundedIcon fontSize="small" />*/}
                                    {/*</button>*/}
                                </div>
                            </div>
                        </div>
                    ))}

                    {!pedidosFiltrados.length && (
                        <div className="pedidos-page__empty">Sin pedidos</div>
                    )}
                </div>
            )}

            <Dialog open={detailOpen} onClose={cerrarDetalle} fullWidth maxWidth="md">
                <DialogTitle className="pedidos-page__dialog-title">
                    <span>Detalle</span>

                    <button
                        type="button"
                        className="pedidos-page__dialog-close-button"
                        onClick={cerrarDetalle}
                        title="Cerrar"
                        aria-label="Cerrar"
                    >
                        <CloseRoundedIcon fontSize="small" />
                    </button>
                </DialogTitle>

                <DialogContent dividers>
                    {selectedPedido && (
                        <div className="pedidos-page__detail">
                            <div className="pedidos-page__detail-block">
                                <p className="pedidos-page__detail-label">Pedido</p>
                                <p className="pedidos-page__detail-value">{selectedPedido.numeroPedido}</p>
                            </div>

                            <div className="pedidos-page__detail-grid">
                                <div className="pedidos-page__detail-block">
                                    <p className="pedidos-page__detail-label">Cliente</p>
                                    <p className="pedidos-page__detail-value">{selectedPedido.clienteNombre}</p>
                                </div>

                                <div className="pedidos-page__detail-block">
                                    <p className="pedidos-page__detail-label">Restaurante</p>
                                    <p className="pedidos-page__detail-value">{selectedPedido.restauranteNombre}</p>
                                </div>

                                <div className="pedidos-page__detail-block">
                                    <p className="pedidos-page__detail-label">Repartidor</p>
                                    <p className="pedidos-page__detail-value">
                                        {selectedPedido.repartidorNombre || "Sin repartidor"}
                                    </p>
                                </div>

                                <div className="pedidos-page__detail-block">
                                    <p className="pedidos-page__detail-label">Estado</p>
                                    <p className="pedidos-page__detail-value">{selectedPedido.estado}</p>
                                </div>

                                <div className="pedidos-page__detail-block">
                                    <p className="pedidos-page__detail-label">Distancia</p>
                                    <p className="pedidos-page__detail-value">{selectedPedido.distanciaKm} km</p>
                                </div>

                                <div className="pedidos-page__detail-block">
                                    <p className="pedidos-page__detail-label">Tarifa</p>
                                    <p className="pedidos-page__detail-value">{selectedPedido.tipoTarifaDia}</p>
                                </div>
                            </div>

                            <div className="pedidos-page__detail-block">
                                <p className="pedidos-page__detail-label">Entrega</p>
                                <p className="pedidos-page__detail-value">{selectedPedido.direccionEntrega}</p>
                            </div>

                            <div className="pedidos-page__detail-block">
                                <p className="pedidos-page__detail-label">Items</p>
                                <div className="pedidos-page__items">
                                    {selectedPedido.items?.map((item, index) => (
                                        <div className="pedidos-page__item-card" key={`${item.comboId}-${index}`}>
                                            <div>
                                                <p className="pedidos-page__item-card-title">
                                                    #{item.numeroCombo} · {item.nombre}
                                                </p>
                                                <p className="pedidos-page__item-card-text">
                                                    x{item.cantidad} · ₡ {formatoMoneda(item.precioUnitario)}
                                                </p>
                                            </div>
                                            <p className="pedidos-page__item-card-total">
                                                ₡ {formatoMoneda(item.subtotalLinea)}
                                            </p>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            {selectedPedido.factura && (
                                <div className="pedidos-page__detail-block">
                                    <p className="pedidos-page__detail-label">Factura</p>
                                    <div className="pedidos-page__factura">
                                        <div className="pedidos-page__factura-row">
                                            <span>Factura</span>
                                            <strong>{selectedPedido.factura.numeroFactura}</strong>
                                        </div>
                                        <div className="pedidos-page__factura-row">
                                            <span>Subtotal</span>
                                            <strong>₡ {formatoMoneda(selectedPedido.factura.subtotal)}</strong>
                                        </div>
                                        <div className="pedidos-page__factura-row">
                                            <span>Transporte</span>
                                            <strong>₡ {formatoMoneda(selectedPedido.factura.costoTransporte)}</strong>
                                        </div>
                                        <div className="pedidos-page__factura-row">
                                            <span>IVA</span>
                                            <strong>₡ {formatoMoneda(selectedPedido.factura.montoIva)}</strong>
                                        </div>
                                        <div className="pedidos-page__factura-row pedidos-page__factura-row--total">
                                            <span>Total</span>
                                            <strong>₡ {formatoMoneda(selectedPedido.factura.montoTotal)}</strong>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>
                    )}
                </DialogContent>
            </Dialog>
        </div>
    );
}