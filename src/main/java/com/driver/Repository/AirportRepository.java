package com.driver.Repository;

import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AirportRepository {
    HashMap<String, Airport> airportDb=new HashMap<>(); //stores airport name vs airport;
    HashMap<Integer,Flight> flightDb=new HashMap<>();//stores flightId vs flight;
    HashMap<Integer,Passenger> passengerDb=new HashMap<>();
    HashMap<Integer,List<Integer>> flightIdVsPassengerIdsDb=new HashMap<>();//stores flight id vs list of passenger ids,i.e in a particular
    //flight no of passengers
    HashMap<Integer,List<Integer>> passengerIdVsFlightIdsDb=new HashMap<>();//stores passengers Id vs bookings of that passenger over
    //various flightIds


    public void addAirport(Airport airport) {
        airportDb.put(airport.getAirportName(),airport);
    }

    public String getLargestAirportName() {
        List<String> names=new ArrayList<>();
        int max_terminals=0;
        for(String ele:airportDb.keySet()){
            int available_terminals=airportDb.get(ele).getNoOfTerminals();
            if(available_terminals>max_terminals){
                max_terminals=available_terminals;
                names.add(ele);
            }
        }
        Collections.sort(names);
        return names.get(0);
    }

    public String addFlight(Flight flight) {
        flightDb.put(flight.getFlightId(),flight);
        return "SUCCESS";
    }

    public String addPassenger(Passenger passenger) {
        passengerDb.put(passenger.getPassengerId(),passenger);
        return "SUCCESS";
    }

    public double getShortestDurationOfPossibleBetweenTwoCities(City fromCity, City toCity) {
        double ans=Double.MAX_VALUE;
        for(Integer ele:flightDb.keySet()){
            Flight flight=flightDb.get(ele);
            if(flight.getFromCity().equals(fromCity) && flight.getToCity().equals(toCity)){
                double time=flight.getDuration();
                if(time<ans){
                    ans=time;
                }
            }
        }
        return ans==Double.MAX_VALUE?-1:ans;
    }

    public String bookATicket(Integer flightId, Integer passengerId) {
        //If the numberOfPassengers who have booked the flight is greater than : maxCapacity, in that case :
        //return a String "FAILURE"
        //Also if the passenger has already booked a flight then also return "FAILURE".
        //else if you are able to book a ticket then return "SUCCESS"
        if(flightIdVsPassengerIdsDb.get(flightId)!=null && flightIdVsPassengerIdsDb.get(flightId).size()==flightDb.get(flightId).getMaxCapacity()){
            return "FAILURE";
        }
        if(passengerIdVsFlightIdsDb.get(passengerId)!=null){
            for(Integer ele:passengerIdVsFlightIdsDb.get(passengerId)){
                if(ele==flightId) return "FAILURE";
            }
        }

        flightIdVsPassengerIdsDb.putIfAbsent(flightId,new ArrayList<Integer>());
        flightIdVsPassengerIdsDb.get(flightId).add(passengerId);
        passengerIdVsFlightIdsDb.putIfAbsent(passengerId,new ArrayList<Integer>());
        passengerIdVsFlightIdsDb.get(passengerId).add(flightId);
        return "SUCCESS";
    }

    public String cancelATicket(Integer flightId, Integer passengerId) {
        //If the passenger has not booked a ticket for that flight or the flightId is invalid or in any other failure case
        // then return a "FAILURE" message
        // Otherwise return a "SUCCESS" message
        // and also cancel the ticket that passenger had booked earlier on the given flightId
        if(flightDb.get(flightId)==null) return "FAILURE";
        boolean found=false;
        if(passengerIdVsFlightIdsDb.get(passengerId)==null) return "FAILURE";
        for(Integer ele:passengerIdVsFlightIdsDb.get(passengerId)){
            if(ele==flightId) {
                found=true;
                break;
            }
        }
        if(found==false) return "FAILURE";
        passengerIdVsFlightIdsDb.get(passengerId).remove(flightId);
        flightIdVsPassengerIdsDb.get(flightId).remove(passengerId);
        return "SUCCESS";
    }

    public int countOfBookingsDoneByPassengerAllCombined(Integer passengerId) {
        return passengerIdVsFlightIdsDb.get(passengerId).size();
    }

    public int getNumberOfPeopleOn(Date date, String airportName) {
        //Calculate the total number of people who have flights on that day on a particular airport
        //This includes both the people who have come for a flight and who have landed on an airport after their flight
        City currentCity=airportDb.get(airportName).getCity();
        int count=0;
        for(Integer id:flightDb.keySet()){
            Flight flightObj=flightDb.get(id);
            if(flightObj.getFromCity().equals(currentCity)||flightObj.getToCity().equals(currentCity)){
                if(flightObj.getFlightDate().equals(date)){
                   count+=flightIdVsPassengerIdsDb.get(id).size();
                }
            }
        }
        return count;
    }

    public String getAirportNameFromFlightId(Integer flightId) {

        //We need to get the starting airportName from where the flight will be taking off (Hint think of City variable if that can be of some use)
        //return null incase the flightId is invalid or you are not able to find the airportName
        if(flightDb.get(flightId)==null) return null;
        City currentCity=flightDb.get(flightId).getFromCity();
        for(String name:airportDb.keySet()){
            Airport airportObj=airportDb.get(name);
            if(airportObj.getCity().equals(currentCity)) return name;
        }
        return  null;

    }

    public int calculateFlightFare(Integer flightId) {
        //Calculation of flight prices is a function of number of people who have booked the flight already.
        //Price for any flight will be : 3000 + noOfPeopleWhoHaveAlreadyBooked*50
        //Suppose if 2 people have booked the flight already : the price of flight for the third person will be 3000 + 2*50 = 3100
        //This will not include the current person who is trying to book, he might also be just checking price
        int noOfPeopleWhoHaveAlreadyBooked=flightIdVsPassengerIdsDb.get(flightId).size();
        int fare=0;
        fare=3000+noOfPeopleWhoHaveAlreadyBooked*50;
        return  fare;
    }

    public int calculateRevenueOfAFlight(Integer flightId) {
        int totalPassengers=flightIdVsPassengerIdsDb.get(flightId).size();
        int val=totalPassengers-1;
        int total=3000*totalPassengers+50*(val*(val+1)/2);
        return total;
    }
}
