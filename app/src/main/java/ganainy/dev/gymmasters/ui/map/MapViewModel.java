package ganainy.dev.gymmasters.ui.map;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;

import ganainy.dev.gymmasters.BuildConfig;
import ganainy.dev.gymmasters.R;
import ganainy.dev.gymmasters.models.app_models.Gym;
import ganainy.dev.gymmasters.models.places_models.Candidate;
import ganainy.dev.gymmasters.models.places_models.Candidates;
import ganainy.dev.gymmasters.models.places_models.Geometry;
import ganainy.dev.gymmasters.models.places_models.PlacesApi;
import ganainy.dev.gymmasters.utils.Event;
import ganainy.dev.gymmasters.utils.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MapViewModel extends AndroidViewModel {

    public LiveData<Boolean> getRippleLoadingLiveData() {
        return mRippleLoadingLiveData;
    }

    public LiveData<Event<LatLng>> getLocationLiveData() {
        return mLocationLiveData;
    }

    public LiveData<Event<Gym>> getGymInfoLiveData() {
        return mGymInfoLiveData;
    }

    public LiveData<Event<String>> getToastLiveData() {
        return mToastLiveData;
    }

    private MutableLiveData<Boolean> mRippleLoadingLiveData=new MutableLiveData<>();
    private MutableLiveData<Event<LatLng>> mLocationLiveData=new MutableLiveData<>();
    private MutableLiveData<Event<Gym>> mGymInfoLiveData=new MutableLiveData<>();
    private MutableLiveData<Event<String>> mToastLiveData=new MutableLiveData<>();

    public MapViewModel(@NonNull Application application) {
        super(application);
    }

    public void findNearbyGyms(LatLng latLng) {
        mRippleLoadingLiveData.setValue(true);





        Retrofit retrofit= RetrofitClient.getRetrofitPlacesClient();
        PlacesApi placesApi = retrofit.create(PlacesApi.class);

        /*call places api to find gyms in radius of 5km from my location*/
        Call<Candidates> call = placesApi.getParentObject("gym", "textquery",
                "geometry/location,formatted_address,name,opening_hours,rating", "circle:5000@" +
                        latLng.latitude + "," + latLng.longitude, BuildConfig.PLACES_API_KEY
        );
        call.enqueue(new Callback<Candidates>() {
            @Override
            public void onResponse(@NonNull Call<Candidates> call, @NonNull Response<Candidates> response) {

                try {
                    Candidates body = response.body();
                    Candidate candidate;
                    Gym gym;
                    Boolean openNow = null;

                    if (body!=null && body.getCandidates()!=null){
                        candidate = body.getCandidates().get(0);
                        Geometry geometry = candidate.getGeometry();
                        Double lat = geometry.getLocation().getLat();
                        Double lng = geometry.getLocation().getLng();
                        String formatted_address = candidate.getFormattedAddress();
                        String name = candidate.getName();
                        Double rating = candidate.getRating();
                        if (candidate.getOpeningHours()!=null) openNow = candidate.getOpeningHours().getOpenNow();

                        String openingHours;
                        if (openNow==null){
                             openingHours=getApplication().getString(R.string.unknown);
                        }else if (openNow){
                            openingHours=getApplication().getString(R.string.open_right_now);
                        }else {
                            openingHours=getApplication().getString(R.string.closed_right_now);
                        }

                        gym=new Gym(name,rating,formatted_address,openingHours);

                        mLocationLiveData.setValue(new Event<>(new LatLng(lat,lng)));
                        mGymInfoLiveData.setValue(new Event<>(gym));
                    }else {
                        /*getting location failed*/
                        mToastLiveData.setValue(new Event<>(getApplication().getString(R.string.couldnt_find_nearby_gym)));
                    }
                } catch (Exception e) {
                    Log.d("TAG", "onResponse: "+e.getMessage());
                   e.printStackTrace();
                    mToastLiveData.setValue(new Event<>(getApplication().getString(R.string.couldnt_find_nearby_gym)));
                }finally {
                    mRippleLoadingLiveData.setValue(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Candidates> call, @NonNull Throwable t) {
                mRippleLoadingLiveData.setValue(false);
                mToastLiveData.setValue(new Event<>(getApplication().getString(R.string.couldnt_find_nearby_gym)));
            }
        });

    }

}
