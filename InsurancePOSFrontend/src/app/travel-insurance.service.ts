import { Injectable } from '@angular/core';
import { Headers,Response, Http } from '@angular/http';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs/Rx';

import {TravelInsurance} from './travelInsurance.interface';
import {Policy} from './policy.interface';
import {RiskItem} from './riskItem.interface';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import { TravelinsformComponent } from './travelinsform/travelinsform.component';

@Injectable()
export class TravelInsuranceService {
  private baseUrl = 'http://localhost:8090';
  private headers = new HttpHeaders({'Content-Type': 'application/json'});

  constructor(private http: Http, private httpClient: HttpClient) { }

  getTravelRegions(): Observable<RiskItem[]> {
    return this.http.get(this.baseUrl + '/travelInsurance/getRegions/')
    .map((response:Response) => { return response.json()})
      .catch((error:any) => Observable.throw(error.json().error || 'Server error'));
  }

  getAges() : Observable<RiskItem[]> {
    return this.http.get(this.baseUrl + '/travelInsurance/getAges/')
    .map((response:Response) => {return response.json()})
      .catch((error:any) => Observable.throw(error.json().error || 'Server error'));
  }

  getSports() : Observable<RiskItem[]> {
    return this.http.get(this.baseUrl + '/travelInsurance/getSports/')
    .map((response:Response) => {return response.json()})
      .catch((error:any) => Observable.throw(error.json().error || 'Server error'));
  }

  getInsuranceValues() : Observable<RiskItem[]> {
    return this.http.get(this.baseUrl + '/travelInsurance/getInsuranceValues/')
    .map((response:Response) => {return response.json()})
      .catch((error:any) => Observable.throw(error.json().error || 'Server error'));
  }

  createInsurance(travelInsurance: TravelInsurance) : Observable<number> {
    return this.httpClient.post(this.baseUrl + '/travelInsurance/createTravelInsurance/', travelInsurance, {headers: this.headers})
    .map((response:Response) => {return response})
    .catch((error:any) => Observable.throw(error || 'Server error'));
  }

 
}