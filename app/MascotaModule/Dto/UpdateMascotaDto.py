class UpdateMascotaDto:

    ESTADOS_VALIDOS = ["PERDIDA", "ENCONTRADA", "NORMAL", "CELO", "ADOPCION"]

    @staticmethod
    def from_request(data):

        estado = data.get("estado")

        if estado and estado not in UpdateMascotaDto.ESTADOS_VALIDOS:
            raise ValueError("Estado de mascota inválido.")

        return {
            "nombre": data.get("nombre"),
            "especie": data.get("especie"),
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