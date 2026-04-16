import { AfterViewInit, Component, Input } from '@angular/core';
import * as L from 'leaflet';
@Component({
  selector: 'app-mapa',
  standalone: false,
  templateUrl: './mapa.html',
  styleUrl: './mapa.css',
})

export class MapaComponent implements AfterViewInit {

  @Input() coordenadas: { lat: number, lng: number, label?: string }[] = [];

  private map!: L.Map;

  ngAfterViewInit(): void {
    this.initMap();
  }

  private initMap(): void {
    this.map = L.map('map').setView([20.5888, -100.3899], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap'
    }).addTo(this.map);

    this.dibujarMarcadores();
  }

  private dibujarMarcadores(): void {
    this.coordenadas.forEach(c => {
      L.marker([c.lat, c.lng])
        .addTo(this.map)
        .bindPopup(c.label || 'Ubicación');
    });
  }
}