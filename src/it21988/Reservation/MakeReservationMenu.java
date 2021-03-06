package it21988.Reservation;

import it21988.House.House;
import it21988.User.Owner;
import it21988.User.Renter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Scanner;

import static it21988.House.House.housesList;
import static it21988.Reservation.Reservation.reservationSet;
import static it21988.User.Owner.createOwner;
import static it21988.User.User.inputTaxNumber;
import static it21988.User.User.userExists;




public class MakeReservationMenu {


    static Scanner input;
    static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    ArrayList<String> housesNotAvailable;
    public MakeReservationMenu(Scanner input){
        this.input=input;
        housesNotAvailable = new ArrayList<>();
        LocalDate[] dates=setDate(input);

        getNotAvailableHousesByDate(dates[0],dates[1]);
        getNotAvailableHousesByPeopleNumber(inputPplNumber(),inputWantsToSeeBigger());
        getNotAvailableHousesByMunicipality(inputMunicipality());
        int numberOfHouses=showAvailableHouses();
        if (numberOfHouses>0)
            inputMakeReservationOrNot(dates);
        else
            System.out.println("No houses found with these criteria.");

    }




    public static LocalDate[] setDate(Scanner input){

        LocalDate systemTime= LocalDate.now();
        LocalDate startDate =null, endDate =null;
        String inputStartDate= null, inputEndDate =null;

        boolean correct=false;
        do {
            correct=true;
            System.out.print("Provide the dates,\nStart: ");
            try {
                inputStartDate=input.nextLine();
                startDate= LocalDate.parse(inputStartDate, dtf);

                if (startDate.isEqual(systemTime) || startDate.isBefore(systemTime))        //Checks startDate is>= systemDate
                    throw new IllegalStateException();

                System.out.print("End: ");

                inputEndDate=input.nextLine();
                endDate= LocalDate.parse(inputEndDate, dtf);

                if (endDate.isEqual(systemTime) || endDate.isBefore(systemTime))
                    throw new IllegalCurrentDateEqualityException();

                if (ChronoUnit.DAYS.between(startDate,endDate)>15)
                    throw new DatesRangeException();

                if(startDate.isAfter(endDate) || startDate.isEqual(endDate)){
                    throw new IllegalDateEquallityException();
                }

            }catch (DateTimeParseException ex) {
                System.out.println("Date format must be DD/MM/YYYY");
                correct=false;
            }catch (IllegalCurrentDateEqualityException | DatesRangeException
                    | IllegalDateEquallityException  ex) {
                correct=false;
            }
        }while (!correct);

        return new LocalDate[]{startDate, endDate};
    }

    private void inputMakeReservationOrNot(LocalDate[] dates) {
        Byte choice = 0;
        System.out.println("""
               Press:
               1. Make reservation
               2. Previous Menu
               """);
        try {
            choice = Byte.parseByte(input.nextLine().trim());
            if (choice == 1){
                makeReservation(dates);
            }
        } catch (NumberFormatException nfe) {System.out.println("Wrong input.");}


    }

    private void makeReservation(LocalDate[] dates) {
        int taxNumber=checkUserExists();
        String houseID=inputHouseID(taxNumber);
        int cost=0;
        if (houseID.equals("WW0000"))
            return;
        String reservationID=null;
        int counter=0;
        boolean found;
        do {
            found=false;
            for (Reservation reservation : reservationSet) {                 //Sets the reservationID
                reservationID = houseID.substring(0, 2) + dates[0].getYear() + String.format("%05d", counter);
                if (reservation.getReservationID().equals(reservationID)) {
                    found=true;                                             //if it finds the id it
                    counter++;                                              //adds one to the end of the id
                    break;                                                  //and starts again until it doesn't match, not optimized
                }
            }
        }while (found);

        reservationSet.add(new Reservation(reservationID, houseID, taxNumber, dates[0], dates[1]));
        for (House house :housesList){
            if (houseID.equals(house.houseID())){
                cost=(dates[1].compareTo(dates[0]))*house.dailyCost();
                break;
            }
        }
        System.out.println("\nReservation ID: #"+reservationID +"\nDates reserved "+ dates[0].format(dtf)+" - "+ dates[1].format(dtf)+"\nTotal cost: "+cost +"\u20ac");

    }

    private int checkUserExists(){
        int taxNumber=inputTaxNumber();
        if(userExists(taxNumber)) {
            if (!Renter.renterExists(taxNumber)) {
                System.out.print("Account already exists as Owner, to get Renter access provide ");
                Owner.createOwnerRenterExists(taxNumber);
            }
        } else {
            System.out.println("Provide the following info to register as Renter.");
            createOwner(taxNumber);
        }
        return taxNumber;
    }

    private String inputHouseID(int taxNumber){
        boolean correct=false;
        System.out.print("Enter House ID to be booked: ");
        String houseID= null;
        int houseOwnerTaxNumber=0;
        do{
            houseID=input.nextLine();
            if (houseID.matches("[A-Z]{2}[0-9]{4}")){
                for (House house :housesList) {
                    if (house.houseID().equals(houseID)){
                        houseOwnerTaxNumber= house.taxNumber();
                        correct = true;
                        if (houseOwnerTaxNumber==taxNumber){        //If renter is also owner of the house
                            System.out.println("You can't rent an apartment you own.");
                            return "WW0000";
                        }
                        break;
                    }
                }
            }else{
                System.out.println("House ID must be of format LLNNNN (L= letter, N= number) and exist.");
            }

        }while(!correct);
        return houseID;
    }
    private int showAvailableHouses(){
        int numberOfHouses=0;
        for (House house : housesList){
            if (!housesNotAvailable.contains(house.houseID())){
                System.out.println("House ID: #" + house.houseID()+"\nDaily cost: "+house.dailyCost()+"\u20ac\n");
                numberOfHouses++;
            }
        }
        return numberOfHouses;
    }

    /*
    * Checks for every house if it is not available at specified date,
    * if it's not available puts it in the list.
    * */

    private void getNotAvailableHousesByDate(LocalDate startDate,LocalDate endDate) {

        for (Reservation reservation : reservationSet){
            if (startDate.isBefore(reservation.getStartDateBooked()) && endDate.isBefore(reservation.getStartDateBooked()) || (startDate.isAfter(reservation.getEndDateBooked()) && endDate.isAfter(reservation.getEndDateBooked())) ){
                housesNotAvailable.remove(reservation.getHouseID());
            }else{
                housesNotAvailable.add(reservation.getHouseID());
            }
        }
    }

    private void getNotAvailableHousesByPeopleNumber(byte pplNumber, boolean wantsToSeeBigger){
        if (wantsToSeeBigger){
            for (House house :housesList){
                if (house.pplNumber()<pplNumber-1 || house.pplNumber()>pplNumber+1){
                    housesNotAvailable.add(house.houseID());
                }
            }
        }else {
            for (House house : housesList) {
                if (house.pplNumber() != pplNumber) {
                    housesNotAvailable.add(house.houseID());
                }
            }
        }
    }

    private void getNotAvailableHousesByMunicipality(String municipality){
        for (House house : housesList){
            if (!house.municipality().equals(municipality)){
                housesNotAvailable.add(house.houseID());
            }
        }
    }


    public  byte inputPplNumber() {
        byte num = 0;
        boolean correct = false;
        do {
            try {
                do {
                    System.out.print("Number of people: ");
                    correct = (num = Byte.parseByte(input.nextLine())) > 0;
                    if (correct) break;
                    System.out.println("Provide positive whole number!");
                } while (!correct);
            } catch (NumberFormatException nfe) {
                System.out.println("You didnt provide a number or too big number (max. 127).");
            }
        } while (!correct);

        return num;
    }

    private String inputMunicipality() {
        System.out.print("Municipality: ");
        String municipality;

        while (!(municipality = input.nextLine()).trim().matches("[A-Z][A-Za-z '.]{2,50}")) {
            System.out.println("Municipality must be 2-50 letters, can also contain special characters .' .");
            System.out.print("Municipality: ");
        }
        return municipality;
    }

    private boolean inputWantsToSeeBigger() {
        String answer;
        do {
            System.out.println("Include houses bigger/ smaller by 1 person?");
        }while(!(answer=input.nextLine().trim().toUpperCase()).matches("[YN]"));

        return answer.equals("Y") ? true : false;
    }

}
