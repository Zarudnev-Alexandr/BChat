import httpx


async def get_coordinates_from_dadata(address: str):
    """Получить координаты из API Dadata по адресу"""

    # Замените 'YOUR_DADATA_API_KEY' на ваш собственный API-ключ от Dadata
    dadata_api_key = "3c6b36506c41a1645c09ef05b4eb93c56df2bda9"
    secret_api_key = "ec69fca574ef007ff3e762502ead6070be1db9c4"
    dadata_url = "https://cleaner.dadata.ru/api/v1/clean/address"

    async with httpx.AsyncClient() as client:
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Token {dadata_api_key}",
            "X-Secret": secret_api_key,
        }

        data = [address]  # Обратите внимание, что данные передаются в виде списка
        # Используйте json вместо content для корректной сериализации данных
        response = await client.post(dadata_url, json=data, headers=headers)

        if response.status_code == 200:
            result = response.json()
            coordinates = result[0].get("geo_lat"), result[0].get("geo_lon")

            # Преобразовываем координаты в числовой формат
            geoposition_latitude = (
                float(coordinates[0]) if coordinates and coordinates[0] else None
            )
            geoposition_longitude = (
                float(coordinates[1]) if coordinates and coordinates[1] else None
            )

            return {
                "geoposition_latitude": geoposition_latitude,
                "geoposition_longitude": geoposition_longitude,
            }

    # Если запрос к Dadata не удался, возвращаем пустые координаты
    return {
        "geoposition_latitude": 0,
        "geoposition_longitude": 0,
    }
