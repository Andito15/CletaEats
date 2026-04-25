import { useEffect, useRef, useState } from "react";
import MyLocationRoundedIcon from "@mui/icons-material/MyLocationRounded";
import PlaceRoundedIcon from "@mui/icons-material/PlaceRounded";
import { loadGoogleMaps } from "../utils/loadGoogleMaps";
import "./LocationPickerMap.css";

const DEFAULT_CENTER = { lat: 9.9281, lng: -84.0907 };

export default function LocationPickerMap({
                                              latitud,
                                              longitud,
                                              direccion,
                                              onChange,
                                          }) {
    const mapRef = useRef(null);
    const mapInstanceRef = useRef(null);
    const markerRef = useRef(null);
    const geocoderRef = useRef(null);

    const [mapError, setMapError] = useState("");
    const [locating, setLocating] = useState(false);

    useEffect(() => {
        let isMounted = true;

        const initMap = async () => {
            try {
                await loadGoogleMaps();
                if (!isMounted || !mapRef.current || !window.google?.maps) return;

                const center =
                    latitud !== null && latitud !== "" && longitud !== null && longitud !== ""
                        ? { lat: Number(latitud), lng: Number(longitud) }
                        : DEFAULT_CENTER;

                const map = new window.google.maps.Map(mapRef.current, {
                    center,
                    zoom:
                        latitud !== null && latitud !== "" && longitud !== null && longitud !== ""
                            ? 17
                            : 13,
                    mapTypeControl: false,
                    streetViewControl: false,
                    fullscreenControl: false,
                });

                const marker = new window.google.maps.Marker({
                    position: center,
                    map:
                        latitud !== null && latitud !== "" && longitud !== null && longitud !== ""
                            ? map
                            : null,
                    draggable: false,
                });

                const geocoder = new window.google.maps.Geocoder();

                map.addListener("click", async (e) => {
                    if (!e.latLng) return;

                    const nextLat = Number(e.latLng.lat().toFixed(7));
                    const nextLng = Number(e.latLng.lng().toFixed(7));

                    marker.setPosition({ lat: nextLat, lng: nextLng });
                    marker.setMap(map);

                    try {
                        const result = await geocoder.geocode({
                            location: { lat: nextLat, lng: nextLng },
                        });

                        const formatted = result.results?.[0]?.formatted_address || direccion || "";

                        onChange({
                            latitud: nextLat,
                            longitud: nextLng,
                            direccion: formatted,
                        });
                    } catch {
                        onChange({
                            latitud: nextLat,
                            longitud: nextLng,
                            direccion: direccion || "",
                        });
                    }
                });

                mapInstanceRef.current = map;
                markerRef.current = marker;
                geocoderRef.current = geocoder;
                setMapError("");
            } catch (err) {
                setMapError(err.message || "No se pudo cargar el mapa");
            }
        };

        initMap();

        return () => {
            isMounted = false;
        };
    }, []);

    useEffect(() => {
        const map = mapInstanceRef.current;
        const marker = markerRef.current;

        if (!map || !marker) return;
        if (latitud === null || latitud === "" || longitud === null || longitud === "") return;

        const position = {
            lat: Number(latitud),
            lng: Number(longitud),
        };

        marker.setPosition(position);
        marker.setMap(map);
        map.panTo(position);
    }, [latitud, longitud]);

    const usarUbicacionActual = () => {
        if (!navigator.geolocation) {
            setMapError("Geolocalización no disponible");
            return;
        }

        setLocating(true);
        setMapError("");

        navigator.geolocation.getCurrentPosition(
            async (position) => {
                const nextLat = Number(position.coords.latitude.toFixed(7));
                const nextLng = Number(position.coords.longitude.toFixed(7));

                const map = mapInstanceRef.current;
                const marker = markerRef.current;
                const geocoder = geocoderRef.current;

                if (map && marker) {
                    const point = { lat: nextLat, lng: nextLng };
                    marker.setPosition(point);
                    marker.setMap(map);
                    map.setZoom(17);
                    map.panTo(point);
                }

                try {
                    if (geocoder) {
                        const result = await geocoder.geocode({
                            location: { lat: nextLat, lng: nextLng },
                        });

                        const formatted = result.results?.[0]?.formatted_address || direccion || "";

                        onChange({
                            latitud: nextLat,
                            longitud: nextLng,
                            direccion: formatted,
                        });
                    } else {
                        onChange({
                            latitud: nextLat,
                            longitud: nextLng,
                            direccion: direccion || "",
                        });
                    }
                } catch {
                    onChange({
                        latitud: nextLat,
                        longitud: nextLng,
                        direccion: direccion || "",
                    });
                } finally {
                    setLocating(false);
                }
            },
            () => {
                setLocating(false);
                setMapError("No se pudo obtener ubicación");
            },
            {
                enableHighAccuracy: true,
                timeout: 10000,
            }
        );
    };

    return (
        <div className="location-picker-map">
            <div className="location-picker-map__toolbar">
                <button
                    type="button"
                    className="location-picker-map__icon-button location-picker-map__icon-button--primary"
                    onClick={usarUbicacionActual}
                    title="Usar ubicación actual"
                    aria-label="Usar ubicación actual"
                    disabled={locating}
                >
                    <MyLocationRoundedIcon fontSize="small" />
                </button>

                <button
                    type="button"
                    className="location-picker-map__icon-button location-picker-map__icon-button--accent"
                    title="Marcar en mapa"
                    aria-label="Marcar en mapa"
                >
                    <PlaceRoundedIcon fontSize="small" />
                </button>
            </div>

            <div ref={mapRef} className="location-picker-map__canvas" />

            {mapError && <p className="location-picker-map__error">{mapError}</p>}
        </div>
    );
}