import { AfterViewInit, Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import * as L from 'leaflet';

@Component({
  selector: 'app-mapa',
  standalone: false,
  templateUrl: './mapa.html',
  styleUrls: ['./mapa.css']
})
export class MapaComponent implements AfterViewInit, OnChanges {
  @Input() coordenadas: { lat: number; lng: number; label?: string }[] = [];
  @Input() center?: { lat: number; lng: number };
  @Input() zoom: number = 13;

  private map!: L.Map;
  private markers: L.Marker[] = [];

  ngAfterViewInit(): void {
    this.initMap();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.map && changes['coordenadas']) {
      this.dibujarMarcadores();
      this.ajustarVista();
    }
  }

  private initMap(): void {
    const centerLatLng = this.center
      ? [this.center.lat, this.center.lng]
      : [20.5888, -100.3899];
    this.map = L.map('map').setView(centerLatLng as L.LatLngExpression, this.zoom);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap'
    }).addTo(this.map);

    this.dibujarMarcadores();
    this.ajustarVista();
  }

  private dibujarMarcadores(): void {
    // Limpiar marcadores existentes
    this.markers.forEach(marker => marker.remove());
    this.markers = [];

    this.coordenadas.forEach(c => {
      const marker = L.marker([c.lat, c.lng]).addTo(this.map);
      if (c.label) {
        marker.bindPopup(c.label);
      }
      this.markers.push(marker);
    });
  }

  private ajustarVista(): void {
    if (this.coordenadas.length === 0) return;
    if (this.coordenadas.length === 1) {
      const c = this.coordenadas[0];
      this.map.setView([c.lat, c.lng], this.zoom);
    } else {
      const group = L.featureGroup(
        this.markers.map(m => L.marker(m.getLatLng()))
      );
      const bounds = group.getBounds();
      this.map.fitBounds(bounds);
    }
  }
}