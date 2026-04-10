class CreateMascotaDto:

    ESTADOS_VALIDOS = ["PERDIDA", "ENCONTRADA", "NORMAL", "CELO", "ADOPCION"]

    @staticmethod
    def from_request(data):

        if "especie" not in data:
            raise ValueError("La especie es obligatoria.")

        estado = data.get("estado", "NORMAL")

        if estado not in CreateMascotaDto.ESTADOS_VALIDOS:
            raise ValueError("Estado de mascota inválido.")

        return {
            "nombre": data.get("nombre"),
            "especie": data["especie"],
            "raza": data.get("raza"),
            "color": data.get("color"),
            "sexo": data.get("sexo"),
            "edad": data.get("edad"),
            "descripcion": data.get("descripcion"),
            "estado": estado,
            "direccion_hogar": data.get("direccion_hogar"),
            "latitud_hogar": data.get("latitud_hogar"),
            "longitud_hogar": data.get("longitud_hogar")
        }