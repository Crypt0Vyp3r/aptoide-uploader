/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.retrofit.request;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;
import pt.caixamagica.aptoide.uploader.webservices.json.ListCategoriesJson;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by neuro on 16-02-2015.
 */
public class ListCategoriesRequest
    extends RetrofitSpiceRequest<ListCategoriesJson, ListCategoriesRequest.Webservice> {

  @Getter @Setter private String mode;
  @Getter @Setter private String language;

  public ListCategoriesRequest() {
    super(ListCategoriesJson.class, ListCategoriesRequest.Webservice.class);
  }

  @Override public ListCategoriesJson loadDataFromNetwork() throws Exception {
    HashMap<String, String> parameters = new HashMap<String, String>();

    parameters.put("mode", mode);
    parameters.put("lang", language);

    return getService().listCategories(parameters);
  }

  public interface Webservice {

    @FormUrlEncoded @POST("/2/listCategories") ListCategoriesJson listCategories(
        @FieldMap HashMap<String, String> args);
  }
}
