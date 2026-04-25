import axios from "axios";

const host = window.location.hostname;

const api = axios.create({
    baseURL: `http://${host}:8080`,
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default api;