
import request from "@/utils/request.js";
export const getWeatherService=(longitude, latitude)=>{
    return request.get('/api/forum/weather',{
        params:{
            longitude,
            latitude
        }
    })
}