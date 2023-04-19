import java.net.HttpURLConnection ;
import java.net.StandardSocketOptions;
import java.net.URL ;

import org.json.simple.parser.JSONParser ;
import org.json.simple.JSONArray ;
import org.json.simple.JSONObject ;

import java.util.Scanner ;
import java.util.HashMap ;
import java.util.ArrayList ;
import java.util.List ;



import java.util.Objects;
import com.fasterxml.jackson.databind.ObjectMapper;


public class Main {

    public static void main(String[] args) {


        // Web Siteden gerekli veriyi çekmek
        try {

            String apiUrl = "https://seffaflik.epias.com.tr/transparency/service/market/intra-day-trade-history?endDate=2022-02-06&startDate=2022-02-06" ;
            URL url = new URL(apiUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            //Check if connect is made
            int responseCode = conn.getResponseCode();

            // 200 OK
            if (responseCode != 200) {
                // That guy throw the our man made Error stage
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            }

            else {
                // Web site to string converion
                StringBuilder informationString = new StringBuilder();
                Scanner scanner = new Scanner(url.openStream());

                informationString.append(scanner.nextLine());

                // End of the string conversion
                scanner.close();

                String jsonString = informationString.toString() ;

                // Some bulshit
                String[] splitedString = jsonString.split("\"intraDayTradeHistoryList\":") ;

                String rawData = splitedString[1] ;
                int end = rawData.indexOf("]");
                String data = rawData.substring(0,end+1) ;

        // Web siteden gereken veriyi sonunda çektik aq
        // ----------------------------------------------------

        // Aldığımız verileri arrayin içine atmayı başardık

                JSONParser jsonParser = new JSONParser() ;

                JSONArray jsonArray = (JSONArray)jsonParser.parse(data) ;
        // -------------------------------------------------------


        // Hash map method denememiz

                List<String> conractList = new ArrayList<String>() ;
                HashMap<String, Double> totalOperAmount = new HashMap<String, Double>() ;
                HashMap<String, Double> totalOperCount = new HashMap<String, Double>() ;
                HashMap<String, Double> weightAvrgPrice = new HashMap<String, Double>() ;

                boolean isFinish = false ;
                int index = 0 ;
                String allowenceKey = "PH" ;

                while (!isFinish) {


                    // Hala liste içinde olduğumuza dair hızlı bir kontrol
                    try {
                        JSONObject indexObject = (JSONObject)jsonArray.get(index) ;
                        index++ ;

                        String conractValue = (String) indexObject.get("conract");

                        String objectKey = conractValue.substring(0,2) ;
                        String objectValue = conractValue.substring(2);

                        // Verini PH içerip içermediğine bakıyoruz
                        if (objectKey.equals(allowenceKey)){

                            // Sonra daha önceden kullanıp kullanmadığımıza
                            if (conractList.contains(objectValue)){

                                double pastValOprtAmnt = totalOperAmount.get(objectValue) ;
                                double pastValOprtCnt = totalOperCount.get(objectValue) ;
                                double pastValAvrgPrice = weightAvrgPrice.get(objectValue) ;

                                try {
                                    double priceVal = (double) indexObject.get("price");
                                    long quantVal = (long) indexObject.get("quantity") ;

                                    double newValOprtAmnt = pastValOprtAmnt + (priceVal*quantVal/10.0) ;
                                    double newValOprtCnt = pastValOprtCnt + (quantVal/10.0) ;
                                    double newValAvrgPrice = pastValAvrgPrice + newValOprtAmnt/newValOprtCnt ;

                                    totalOperAmount.replace(objectValue,newValOprtAmnt);
                                    totalOperCount.replace(objectValue,newValOprtCnt);
                                    weightAvrgPrice.replace(objectValue,newValAvrgPrice);
                                }catch (ClassCastException exception){
                                    long priceVal = (long) indexObject.get("price");
                                    long quantVal = (long) indexObject.get("quantity") ;

                                    double newValOprtAmnt = pastValOprtAmnt + (priceVal*quantVal/10.0) ;
                                    double newValOprtCnt = pastValOprtCnt + (quantVal/10.0) ;
                                    double newValAvrgPrice = pastValAvrgPrice + newValOprtAmnt/newValOprtCnt ;

                                    totalOperAmount.replace(objectValue,newValOprtAmnt);
                                    totalOperCount.replace(objectValue,newValOprtCnt);
                                    weightAvrgPrice.replace(objectValue,newValAvrgPrice);
                                }



                            } else {

                                conractList.add(objectValue) ;

                                try {
                                    double priceVal = (double) indexObject.get("price");
                                    long quantVal = (long) indexObject.get("quantity") ;

                                    double newValOprtAmnt = priceVal*quantVal/10.0 ;;
                                    double newValOprtCnt = quantVal/10.0 ;
                                    double newValAvrgPrice = newValOprtAmnt/newValOprtCnt ;

                                    totalOperAmount.put(objectValue,newValOprtAmnt);
                                    totalOperCount.put(objectValue,newValOprtCnt);
                                    weightAvrgPrice.put(objectValue,newValAvrgPrice);
                                }catch (ClassCastException exception) {
                                    long priceVal = (long) indexObject.get("price");
                                    long quantVal = (long) indexObject.get("quantity") ;

                                    double newValOprtAmnt = priceVal*quantVal/10.0 ;;
                                    double newValOprtCnt = quantVal/10.0 ;
                                    double newValAvrgPrice = newValOprtAmnt/newValOprtCnt ;

                                    totalOperAmount.put(objectValue,newValOprtAmnt);
                                    totalOperCount.put(objectValue,newValOprtCnt);
                                    weightAvrgPrice.put(objectValue,newValAvrgPrice);
                                }


                            }

                        }


                    }catch (IndexOutOfBoundsException error){
                        isFinish = true ;
                    }


                }

                System.out.println(totalOperAmount);
                System.out.println(totalOperCount);
                System.out.println(weightAvrgPrice);





            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
























































































