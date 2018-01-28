package InsurancePOSService.demo.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import InsurancePOSService.demo.models.CarInsuranceDTO;
import InsurancePOSService.demo.models.Client;
import InsurancePOSService.demo.models.HomeInsurance;
import InsurancePOSService.demo.models.HomeInsuranceDTO;
import InsurancePOSService.demo.models.Person;
import InsurancePOSService.demo.models.Policy;
import InsurancePOSService.demo.models.PolicyDTO;
import InsurancePOSService.demo.models.Risk;
import InsurancePOSService.demo.models.RiskItem;
import InsurancePOSService.demo.models.RiskItemDTO;
import InsurancePOSService.demo.models.TravelInsurance;
import InsurancePOSService.demo.models.TravelInsuranceDTO;
import InsurancePOSService.demo.models.VehicleInsurance;

@Controller
@RequestMapping("/carInsurance")
@CrossOrigin("*")
public class CarInsuranceController {
	
	private String urlBase;
	private RestTemplate rest;
	private HttpHeaders headers;
	private Map<String, Object> params;
	private HttpEntity<Map<String, Object>> requestEntity;
	
	public CarInsuranceController(){
		urlBase = "http://localhost:8080/dc/isok/";
		rest = new RestTemplate();
		headers = new HttpHeaders();
	    headers.add("Content-Type", "application/json");
	    headers.add("Accept", "*/*");
		params = new HashMap<String, Object>();
	}
	
	@RequestMapping("/getKilometers")
	@ResponseBody
	public List<RiskItemDTO> getKilometers() {
		return this.getRiskByName("Transport km");
	}
	
	@RequestMapping("/getPrices")
	@ResponseBody
	public List<RiskItemDTO> getPrices() {
		return this.getRiskByName("Repair Price");
	}
	
	@RequestMapping("/getHotelDays")
	@ResponseBody
	public List<RiskItemDTO> getHotelDays() {
		return this.getRiskByName("Hotel Days");
	}
	
	@RequestMapping("/getAltVehicle")
	@ResponseBody
	public List<RiskItemDTO> getAltVehicle() {
		return this.getRiskByName("Alt vehicle");
	}
	
	@RequestMapping(value="/createCarInsurance", method=RequestMethod.POST)
	public ResponseEntity<Double> insuranceValue(@RequestBody CarInsuranceDTO insurance) {
		
		// 	TO DO : Na osnovu dobijenih podataka izracunati cenu samo za osiguranje pomoc na putu i vratiti
 		
		return ResponseEntity.ok(new Double(700));
	}
	
	@RequestMapping(value="/savePolicy", method=RequestMethod.POST)
	public ResponseEntity<String> savePolicy(@RequestBody PolicyDTO policy) {
		
		TravelInsuranceDTO travelInsDTO = policy.getTravelInsurance();
		HomeInsuranceDTO homeInsDTO = policy.getHomeInsurance();
		CarInsuranceDTO carInsDTO = policy.getCarInsurance();
		List<Person> people = policy.getPeople();
		
		Policy policyDal = new Policy();
		Set<Client> clients = new HashSet<Client>();
		Client insuranceOwner = new Client();
		Set<RiskItem> riskItems = new HashSet<RiskItem>();
		
		for(Person p : people){
			if(p.getEmail()!=null && !p.getEmail().trim().isEmpty()){
				insuranceOwner = new Client(p.getFirstName(), p.getLastName(), p.getPassportNumber(), p.getJmbg(), p.getAddress(), p.getTelNum(), p.getEmail(), null, null);
			}
			else
				clients.add(new Client(p.getFirstName(), p.getLastName(), p.getPassportNumber(), p.getJmbg(), p.getAddress(), p.getTelNum(), p.getEmail(), null, null));
		}
		TravelInsurance travelIns = new TravelInsurance(insuranceOwner.getClientEmail(), travelInsDTO.getNumberOfPeople(), 300);	// THIS PRICE
		HttpEntity<TravelInsurance> requestEntity= new HttpEntity<TravelInsurance>(travelIns, this.headers);		
		TravelInsurance travelInsNew = (TravelInsurance) rest.postForObject(this.urlBase+"saveTravelInsurance/", requestEntity, TravelInsurance.class);

		if(homeInsDTO!=null){
			HomeInsurance homeIns = new HomeInsurance(homeInsDTO.getFirstName(), homeInsDTO.getLastName(), String.valueOf(homeInsDTO.getJmbg()), homeInsDTO.getInsuranceLength());
			HttpEntity<HomeInsurance> requestEntity2 = new HttpEntity<HomeInsurance>(homeIns, this.headers);
			HomeInsurance homeInsNew = (HomeInsurance) rest.postForObject(this.urlBase+"saveHomeIns/", requestEntity2, HomeInsurance.class);
			riskItems.add(getRiskById(homeInsDTO.getHomeAge()));
			riskItems.add(getRiskById(homeInsDTO.getHomeSurface()));
			riskItems.add(getRiskById(homeInsDTO.getHomeValue()));
			riskItems.add(getRiskById(homeInsDTO.getInsuranceReason()));
			policyDal.setHomeInsurance(homeInsNew);
		}
		if(carInsDTO.getTypeOfVehicle()!=null && carInsDTO.getYearOfProduction()!=0){
			VehicleInsurance carIns  = new VehicleInsurance(carInsDTO.getTypeOfVehicle(), String.valueOf(carInsDTO.getYearOfProduction()), carInsDTO.getRegTable(), carInsDTO.getChassisNumber(), carInsDTO.getFirstName(), carInsDTO.getLastName(), carInsDTO.getJmbg());	
			HttpEntity<VehicleInsurance> requestEntity3 = new HttpEntity<VehicleInsurance>(carIns, this.headers);
			VehicleInsurance carInsNew = (VehicleInsurance) rest.postForObject(this.urlBase+"saveVehicleInsurance/", requestEntity3, VehicleInsurance.class);
			riskItems.add(getRiskById(carInsDTO.getRepairPrice()));
			riskItems.add(getRiskById(carInsDTO.getNumberOfHotelDays()));
			riskItems.add(getRiskById(carInsDTO.getAlternativeVehicle()));
			riskItems.add(getRiskById(carInsDTO.getNumberOfKm()));
			policyDal.setVehicleInsurance(carInsNew);
		}
		Set<Client> clientsIds = new HashSet<Client>();
		for(Client c : clients){
			HttpEntity<Client> requestEntity4 = new HttpEntity<Client>(c, this.headers);
			Client clientNew = (Client) rest.postForObject(this.urlBase+"saveClient/", requestEntity4, Client.class);
			clientsIds.add(clientNew);
		}
		
		HttpEntity<Client> requestEntity4 = new HttpEntity<Client>(insuranceOwner, this.headers);
		Client insuranceOwnerNew = (Client) rest.postForObject(this.urlBase+"saveClient/", requestEntity4, Client.class);
		clientsIds.add(insuranceOwnerNew);
		
		policyDal.setClients(clientsIds);
		policyDal.setContractStart(travelInsDTO.getStartingDate());
		policyDal.setContractEnd(travelInsDTO.getEndingDate());
		policyDal.setInsuranceOwner(insuranceOwnerNew);
		policyDal.setPriceSummed(589);		// THIS
		policyDal.setTravelInsurance(travelInsNew);

		riskItems.add(getRiskById(travelInsDTO.getAges()));
		riskItems.add(getRiskById(travelInsDTO.getRegion()));
		riskItems.add(getRiskById(travelInsDTO.getSport()));
		riskItems.add(getRiskById(travelInsDTO.getAmmount()));

		
		policyDal.setRiskItems(riskItems);
		HttpEntity<Policy> requestEntity5 = new HttpEntity<Policy>(policyDal, this.headers);
		Policy response = (Policy)rest.postForObject(this.urlBase+"savePolicy/", requestEntity5, Policy.class);
		System.out.println("Response is " + response.getPriceSummed());
		return ResponseEntity.ok(new String("OK"));
		
	}
	
	private void setRequestEntity(Object object){
		this.params = new HashMap<String, Object>();
		params.put("policy", object);
		this.requestEntity= new HttpEntity<Map<String, Object>>(params, this.headers);
	}
	
	private List<RiskItemDTO> getRiskByName(String name){
		this.params.put("name", name);
		 this.requestEntity = new HttpEntity<Map<String, Object>>(this.params, this.headers);
		 Risk responseEntity = (Risk)rest.postForObject(this.urlBase+"RiskName/", this.requestEntity, Risk.class);
		 
		 List<RiskItemDTO> retVal = new ArrayList<RiskItemDTO>();
		 for(RiskItem item : responseEntity.getRiskItems()){
			 retVal.add(new RiskItemDTO(item.getId(), item.getItemName()));
		 }
		 return retVal;
	}
	
	private RiskItem getRiskById(String id){
		this.params.put("id", id);
		this.requestEntity = new HttpEntity<Map<String, Object>>(this.params, this.headers);
		RiskItem item = (RiskItem)rest.postForObject(this.urlBase+"getRiskItem/", this.requestEntity, RiskItem.class);
		return item;
	}
}
