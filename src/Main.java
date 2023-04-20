import java.net.HttpURLConnection ;
import java.net.URL ;

import org.json.simple.parser.JSONParser ;
import org.json.simple.JSONArray ;
import org.json.simple.JSONObject ;

import java.util.Scanner ;
import java.util.HashMap ;
import java.util.ArrayList ;
import java.util.List ;



class ProjectManager {

    public JSONArray dataCollecter(String apiURL){

        try{
            // Try to connect our API Service
            URL url = new URL(apiURL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection() ;
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode() ;

            // Check the our connection establish
            if(responseCode != 200){ // Negative Case
                throw  new RuntimeException("HttpResponseCode: " + responseCode);
            }else{ // Positive Case

                // Now we can pull the all data

                StringBuilder informationString =  new StringBuilder() ;

                Scanner dataScanner = new Scanner(url.openStream()) ;

                informationString.append(dataScanner.nextLine()) ;

                dataScanner.close();

                // After collect the all data we have to transform it into Json Array

                // We scrape the json array on string data type from our data
                String jsonString = informationString.toString() ;

                String[] splitedString = jsonString.split("\"intraDayTradeHistoryList\":") ;

                String rawData  = splitedString[1] ;

                int endIndex = rawData.indexOf("]") ;

                String data = rawData.substring(0, endIndex+1) ;


                // Now we can transform our string into actual JsonArray
                JSONParser jsonParser = new JSONParser() ;

                JSONArray jsonArray = (JSONArray) jsonParser.parse(data) ;

                return  jsonArray ;


            }


        }catch (Exception error){

            error.printStackTrace();
            return new JSONArray() ;

        }}



    public boolean jsonArrayIndexCheck(int indexVal, JSONArray jsonArray){

        // We can check the are we hit end of the list
        try{
            JSONObject tempObj = (JSONObject) jsonArray.get(indexVal) ;
            return false ;

        }catch (IndexOutOfBoundsException exception){
            return true;

        }

        // The reason I create that list to reduce the code inside of the try catch block

    }


    public long priceValueFixer (JSONObject indexObject){

        try{
            double priceVal = (double) indexObject.get("price");

            long fixedPriceVal = (long) priceVal ;

            return fixedPriceVal ;

        }catch (ClassCastException exception){
            long priceVal = (long) indexObject.get("price");

            return priceVal ;
        }

    }

}



public class Main {

    public static void main(String[] args) {

        String apiUrl = "https://seffaflik.epias.com.tr/transparency/service/market/intra-day-trade-history?endDate=2022-02-06&startDate=2022-02-06" ;

        ProjectManager projectManager = new ProjectManager() ;

        JSONArray jsonArray = projectManager.dataCollecter(apiUrl) ;


        // We have to create the our storage list & hast table
        List<String> conractList = new ArrayList<String>() ;
        HashMap<String, Double> totalOperAmount = new HashMap<String, Double>() ;
        HashMap<String, Double> totalOperCount = new HashMap<String, Double>() ;
        HashMap<String, Double> weightAvrgPrice = new HashMap<String, Double>() ;

        // Asign the our control variable
        String allowenceKey = "PH" ;
        boolean isFinish = false ;
        int index = 0 ;

        // Start the our loop to go trough every value
        while (!isFinish) {

            // Get the our json object
            JSONObject indexObject = (JSONObject)jsonArray.get(index) ;
            index++ ;

            // Pull the our conract value
            String conractValue = (String) indexObject.get("conract");

            // Split the our conract value key and object
            String objectKey = conractValue.substring(0,2) ;
            String objectValue = conractValue.substring(2);

            // We check the our key value is it equal to "PH"
            if (objectKey.equals(allowenceKey)){

                // Than we check the is it already in the hast table
                if (conractList.contains(objectValue)){

                    // Get the past value on our table
                    double pastValOprtAmnt = totalOperAmount.get(objectValue) ;
                    double pastValOprtCnt = totalOperCount.get(objectValue) ;
                    double pastValAvrgPrice = weightAvrgPrice.get(objectValue) ;

                    // Get the other values from jsonObject
                    long priceVal = projectManager.priceValueFixer(indexObject) ;
                    long quantVal = (long) indexObject.get("quantity") ;

                    // Calculate the our new values
                    double newValOprtAmnt = pastValOprtAmnt + (priceVal*quantVal/10.0) ;
                    double newValOprtCnt = pastValOprtCnt + (quantVal/10.0) ;
                    double newValAvrgPrice = pastValAvrgPrice + newValOprtAmnt/newValOprtCnt ;

                    // In the end we replace the our value
                    totalOperAmount.replace(objectValue,newValOprtAmnt);
                    totalOperCount.replace(objectValue,newValOprtCnt);
                    weightAvrgPrice.replace(objectValue,newValAvrgPrice);

                } else {

                    // We add the our value in check list
                    conractList.add(objectValue) ;

                    // Get the other values from jsonObject
                    long priceVal = projectManager.priceValueFixer(indexObject) ;
                    long quantVal = (long) indexObject.get("quantity") ;

                    // Calculate the our new values
                    double newValOprtAmnt = priceVal*quantVal/10.0 ;
                    double newValOprtCnt = quantVal/10.0 ;
                    double newValAvrgPrice = newValOprtAmnt/newValOprtCnt ;

                    // Put the new values into our table
                    totalOperAmount.put(objectValue,newValOprtAmnt);
                    totalOperCount.put(objectValue,newValOprtCnt);
                    weightAvrgPrice.put(objectValue,newValAvrgPrice);
                }
            }

            // Let's check the next object is exist
            boolean isHit =  projectManager.jsonArrayIndexCheck(index, jsonArray) ;
            if (isHit){
                isFinish = true ;
            }
        }
        // System print
        System.out.println(totalOperAmount);
        System.out.println(totalOperCount);
        System.out.println(weightAvrgPrice);

    }

}

























































































