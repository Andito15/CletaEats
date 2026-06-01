import {BrowserRouter, HashRouter, Navigate, Route, Routes} from "react-router-dom";
import LoginPage from "../pages/auth/LoginPage";
import DashboardPage from "../pages/dashboard/DashboardPage";
import UsuariosPage from "../pages/usuarios/UsuariosPage";
import RestaurantesPage from "../pages/restaurantes/RestaurantesPage";
import CombosPage from "../pages/combos/CombosPage";
import PedidosPage from "../pages/pedidos/PedidosPage";
import QuejasPage from "../pages/quejas/QuejasPage";
import AdminLayout from "../layout/AdminLayout";

function PrivateRoute({ children }) {
    const token = localStorage.getItem("token");
    return token ? children : <Navigate to="/" replace />;
}

export default function AppRouter() {
    return (
        <HashRouter>
            <Routes>
                <Route path="/" element={<LoginPage />} />

                <Route
                    element={
                        <PrivateRoute>
                            <AdminLayout />
                        </PrivateRoute>
                    }
                >
                    <Route path="/dashboard" element={<DashboardPage />} />
                    <Route path="/usuarios" element={<UsuariosPage />} />
                    <Route path="/restaurantes" element={<RestaurantesPage />} />
                    <Route path="/combos" element={<CombosPage />} />
                    <Route path="/pedidos" element={<PedidosPage />} />
                    <Route path="/quejas" element={<QuejasPage />} />
                </Route>
            </Routes>
        </HashRouter>
    );
}