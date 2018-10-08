package cordova.plugin.paytmpayment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.Toast;

import com.paytm.pgsdk.Log;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class PaytmActivity extends Activity implements PaytmPaymentTransactionCallback{

    String TYPE;

    String STATUS;
    String RESPMSG;
    String CHECKSUMHASH;
    String ORDERID;
    String TXNAMOUNT;
    String RESPCODE;
    String MID;
    String CURRENCY;

    Map<String, String> paramMap;
    PaytmPGService Service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // String package_name = getApplication().getPackageName();
        // setContentView(getApplication().getResources().getIdentifier("activity_paytm", "layout", package_name));
	Intent intent= getIntent();

	Log.d("Paytm Payment", "Reached");
	Log.d("Paytm Payment", intent.getStringExtra("CHECKSUMHASH"));
        paramMap = new HashMap<String, String>();

        TYPE = intent.getStringExtra("TYPE");

        paramMap.put("MID", intent.getStringExtra("MID"));
        paramMap.put("ORDER_ID", intent.getStringExtra("ORDER_ID"));
        paramMap.put("CUST_ID", intent.getStringExtra("CUST_ID"));
        paramMap.put("CHANNEL_ID", intent.getStringExtra("CHANNEL_ID"));
        paramMap.put("TXN_AMOUNT", intent.getStringExtra("TXN_AMOUNT"));
        paramMap.put("WEBSITE", intent.getStringExtra("WEBSITE"));
        paramMap.put("CALLBACK_URL" , intent.getStringExtra("CALLBACK_URL"));
        paramMap.put("CHECKSUMHASH" , intent.getStringExtra("CHECKSUMHASH"));
        paramMap.put("INDUSTRY_TYPE_ID", intent.getStringExtra("INDUSTRY_TYPE_ID"));

        Log.d("Paytm Payment", paramMap.toString());
        paymentMethod();
    }

    private void paymentMethod(){
        if(TYPE.equals("staging")){
            Service = PaytmPGService.getStagingService();
            Log.d("Paytm Payment", "Staging Service");
        }else{
            Service = PaytmPGService.getProductionService();
            Log.d("Paytm Payment", "Prod Service");
        }

        PaytmOrder Order = new PaytmOrder(paramMap);

        Log.e("checksum ", paramMap.toString());
        Service.initialize(Order, null);
        // start payment service call here
        Service.startPaymentTransaction(PaytmActivity.this, true, true, PaytmActivity.this);
    }

    @Override
    public void onTransactionResponse(Bundle bundle) {
        Log.e("checksum ", " respon true " + bundle.toString());
	
	STATUS = bundle.getString("STATUS");
	RESPMSG = bundle.getString("RESPMSG");
	CHECKSUMHASH = bundle.getString("CHECKSUMHASH");
	ORDERID = bundle.getString("ORDERID");
	TXNAMOUNT = bundle.getString("TXNAMOUNT");
	RESPCODE = bundle.getString("RESPCODE");
	MID = bundle.getString("MID");
	CURRENCY = bundle.getString("CURRENCY");	
	onBackPressed();
    }

    @Override
    public void networkNotAvailable() {
        Toast.makeText(this, "Sorry, No internet connection.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void clientAuthenticationFailed(String s) {
        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void someUIErrorOccurred(String s) {
        Log.e("checksum ", " ui fail respon  " + s);
        Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onErrorLoadingWebPage(int i, String s, String s1) {
        Log.e("checksum ", " error loading pagerespon true " + s + "  s1 " + s1);
        Toast.makeText(this, "Error loading page", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressedCancelTransaction() {
        Log.e("checksum ", " cancel call back respon  ");
        Toast.makeText(this, "Transaction cancel", Toast.LENGTH_SHORT).show();
	super.onBackPressed();
    }

    @Override
    public void onTransactionCancel(String s, Bundle bundle) {
        Log.e("checksum ", "  transaction cancel ");
        Toast.makeText(this, "Transaction cancelled", Toast.LENGTH_SHORT).show();
	
	STATUS = bundle.getString("STATUS");
	RESPMSG = bundle.getString("RESPMSG");
	CHECKSUMHASH = bundle.getString("CHECKSUMHASH");
	ORDERID = bundle.getString("ORDERID");
	TXNAMOUNT = bundle.getString("TXNAMOUNT");
	RESPCODE = bundle.getString("RESPCODE");
	MID = bundle.getString("MID");
	CURRENCY = bundle.getString("CURRENCY");

	onBackPressed();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("STATUS", STATUS);
	intent.putExtra("RESPMSG", RESPMSG);
	intent.putExtra("CHECKSUMHASH", CHECKSUMHASH);
	intent.putExtra("ORDERID", ORDERID);
	intent.putExtra("TXNAMOUNT", TXNAMOUNT);
	intent.putExtra("RESPCODE", RESPCODE);
	intent.putExtra("MID", MID);
	intent.putExtra("CURRENCY", CURRENCY);
        setResult(0, intent);
        super.onBackPressed();
    }
}
