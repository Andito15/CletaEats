import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Alert from "@mui/material/Alert";
import CircularProgress from "@mui/material/CircularProgress";
import InputAdornment from "@mui/material/InputAdornment";
import TextField from "@mui/material/TextField";
import AccountCircleRoundedIcon from "@mui/icons-material/AccountCircleRounded";
import LockRoundedIcon from "@mui/icons-material/LockRounded";
import LoginRoundedIcon from "@mui/icons-material/LoginRounded";
import api from "../../api/axios";
import "./LoginPage.css";
import logoCompleto from "../../assets/logo-completo.png";

export default function LoginPage() {
    const navigate = useNavigate();

    const [form, setForm] = useState({
        correo: "",
        password: "",
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleChange = (field, value) => {
        setForm((prev) => ({
            ...prev,
            [field]: value,
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError("");

        try {
            const response = await api.post("/api/auth/login", {
                correo: form.correo.trim(),
                password: form.password,
            });

            const token = response.data?.token;
            if (token) {
                localStorage.setItem("token", token);
            }

            navigate("/dashboard");
        } catch (err) {
            setError(err.response?.data?.message || "No se pudo iniciar sesión");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-page">
            <div className="login-page__overlay" />

            <div className="login-page__card">
                <div className="login-page__brand">
                    <img
                        src={logoCompleto}
                        alt="CletaEats"
                        className="login-page__brand-logo"
                    />

                    <p className="login-page__subtitle">Acceso administrativo</p>
                </div>

                {error && (
                    <Alert severity="error" className="login-page__alert">
                        {error}
                    </Alert>
                )}

                <form className="login-page__form" onSubmit={handleSubmit}>
                    <TextField
                        fullWidth
                        label="Correo"
                        placeholder="example@cletaeats.com"
                        type="email"
                        value={form.correo}
                        onChange={(e) => handleChange("correo", e.target.value)}
                        autoComplete="email"
                        required
                        slotProps={{
                            input: {
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <AccountCircleRoundedIcon />
                                    </InputAdornment>
                                ),
                            },
                        }}
                    />

                    <TextField
                        fullWidth
                        label="Contraseña"
                        placeholder="*********"
                        type="password"
                        value={form.password}
                        onChange={(e) => handleChange("password", e.target.value)}
                        autoComplete="current-password"
                        required
                        slotProps={{
                            input: {
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <LockRoundedIcon />
                                    </InputAdornment>
                                ),
                            },
                        }}
                    />

                    <button
                        type="submit"
                        className="login-page__submit"
                        disabled={loading}
                        aria-label="Iniciar sesión"
                        title="Iniciar sesión"
                    >
                        {loading ? <CircularProgress size={20} color="inherit" /> : <LoginRoundedIcon fontSize="small" />}
                    </button>
                </form>
            </div>
        </div>
    );
}