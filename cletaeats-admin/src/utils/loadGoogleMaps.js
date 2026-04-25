let googleMapsPromise = null;

export function loadGoogleMaps() {
    if (window.google && window.google.maps) {
        return Promise.resolve(window.google.maps);
    }

    if (googleMapsPromise) {
        return googleMapsPromise;
    }

    googleMapsPromise = new Promise((resolve, reject) => {
        const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;

        if (!apiKey) {
            reject(new Error("Falta VITE_GOOGLE_MAPS_API_KEY"));
            return;
        }

        // Callback global requerido por Google
        window.__initGoogleMaps = () => {
            resolve(window.google.maps);
        };

        const script = document.createElement("script");
        script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&v=weekly&loading=async&callback=__initGoogleMaps`;
        script.async = true;
        script.defer = true;
        script.dataset.googleMaps = "true";

        script.onerror = () => reject(new Error("No se pudo cargar Google Maps"));

        document.head.appendChild(script);
    });

    return googleMapsPromise;
}