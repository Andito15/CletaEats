import { useMemo, useState } from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import IconButton from "@mui/material/IconButton";
import MenuIcon from "@mui/icons-material/Menu";
import DashboardRoundedIcon from "@mui/icons-material/DashboardRounded";
import PeopleRoundedIcon from "@mui/icons-material/PeopleRounded";
import StoreRoundedIcon from "@mui/icons-material/StoreRounded";
import FastfoodRoundedIcon from "@mui/icons-material/FastfoodRounded";
import ReceiptLongRoundedIcon from "@mui/icons-material/ReceiptLongRounded";
import ReportRoundedIcon from "@mui/icons-material/ReportRounded";
import LogoutRoundedIcon from "@mui/icons-material/LogoutRounded";
import "./AdminLayout.css";
import logoSolo from "../assets/logo-solo.png";

const menuItems = [
    { label: "Dashboard", path: "/dashboard", icon: <DashboardRoundedIcon /> },
    { label: "Usuarios", path: "/usuarios", icon: <PeopleRoundedIcon /> },
    { label: "Restaurantes", path: "/restaurantes", icon: <StoreRoundedIcon /> },
    { label: "Combos", path: "/combos", icon: <FastfoodRoundedIcon /> },
    { label: "Pedidos", path: "/pedidos", icon: <ReceiptLongRoundedIcon /> },
    { label: "Quejas", path: "/quejas", icon: <ReportRoundedIcon /> },
];

export default function AdminLayout() {
    const [menuOpen, setMenuOpen] = useState(false);
    const navigate = useNavigate();
    const location = useLocation();
    const nombre = localStorage.getItem("adminNombre") || "Admin";

    const initials = useMemo(() => {
        return nombre
            .split(" ")
            .map((word) => word[0])
            .join("")
            .slice(0, 2)
            .toUpperCase();
    }, [nombre]);

    const handleMenuToggle = () => {
        setMenuOpen((prev) => !prev);
    };

    const handleCloseMenu = () => {
        setMenuOpen(false);
    };

    const handleLogout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("adminNombre");
        navigate("/");
    };

    return (
        <div className="admin-layout">
            {menuOpen && (
                <div className="admin-layout__overlay" onClick={handleCloseMenu}></div>
            )}

            <aside
                className={`admin-layout__sidebar ${
                    menuOpen ? "admin-layout__sidebar--open" : ""
                }`}
            >
                <div className="admin-layout__brand">
                    <img
                        src={logoSolo}
                        alt="CletaEats"
                        className="admin-layout__brand-logo"
                    />

                    <div>
                        <h1 className="admin-layout__brand-title">CletaEats</h1>
                        <p className="admin-layout__brand-subtitle">Admin</p>
                    </div>
                </div>

                <hr className="admin-layout__divider" />

                <nav className="admin-layout__nav">
                    {menuItems.map((item) => {
                        const selected = location.pathname === item.path;

                        return (
                            <NavLink
                                key={item.path}
                                to={item.path}
                                onClick={handleCloseMenu}
                                className={`admin-layout__nav-link ${
                                    selected ? "admin-layout__nav-link--active" : ""
                                }`}
                            >
                                <span className="admin-layout__nav-icon">{item.icon}</span>
                                <span className="admin-layout__nav-label">{item.label}</span>
                            </NavLink>
                        );
                    })}
                </nav>

                <div className="admin-layout__logout">
                    <button
                        type="button"
                        className="admin-layout__logout-button"
                        onClick={handleLogout}
                        title="Cerrar sesión"
                        aria-label="Cerrar sesión"
                    >
                        <LogoutRoundedIcon fontSize="small" />
                    </button>
                </div>
            </aside>

            <header className="admin-layout__topbar">
                <div className="admin-layout__topbar-left">
                    <IconButton
                        className="admin-layout__menu-button"
                        onClick={handleMenuToggle}
                        color="inherit"
                    >
                        <MenuIcon />
                    </IconButton>

                    <div className="admin-layout__topbar-brand">
                        <img
                            src={logoSolo}
                            alt="CletaEats"
                            className="admin-layout__topbar-logo"
                        />

                        <div>
                            <p className="admin-layout__topbar-app">CletaEats</p>
                            <p className="admin-layout__topbar-subtitle">Acceso administrativo</p>
                        </div>
                    </div>
                </div>

                <div className="admin-layout__topbar-right">
                    <div className="admin-layout__avatar">{initials}</div>
                    <div className="admin-layout__user-info">
                        <span className="admin-layout__user-name">{nombre}</span>
                        <span className="admin-layout__user-role">Administrador</span>
                    </div>
                </div>
            </header>

            <main className="admin-layout__content">
                <Outlet />
            </main>
        </div>
    );
}