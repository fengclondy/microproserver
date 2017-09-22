package com.ycb.zprovider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.ZhimaMerchantBorrowEntityUploadRequest;
import com.alipay.api.response.ZhimaMerchantBorrowEntityUploadResponse;
import com.ycb.zprovider.constant.GlobalConfig;
import com.ycb.zprovider.mapper.ShopStationMapper;
import com.ycb.zprovider.vo.ShopStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Huo on 2017/9/11.
 * 在线下信用借还场景中，需要商户上传借用实体（如充电宝机柜、借还门店点、借用实物自行车等）
 * 的地址位置及描述信息数据，C端用户可以在芝麻信用借还频道地图页中展示。
 */
@RestController
@RequestMapping("creditupload")
public class CreditUploadController {
    public static final Logger logger = LoggerFactory.getLogger(CreditUploadController.class);
    //初始化alipayClient用到的参数:支付宝网关
    //初始化alipayClient用到的参数:该appId必须设为开发者自己的生活号id
    @Value("${appId}")
    private String appId;
    //初始化alipayClient用到的参数:该私钥为测试账号私钥  开发者必须设置自己的私钥,否则会存在安全隐患
    @Value("${privateKey}")
    private String privateKey;
    //初始化alipayClient用到的参数:仅支持JSON
    @Value("${format}")
    private String format;
    //初始化alipayClient用到的参数:字符编码-传递给支付宝的数据编码
    @Value("${charset}")
    private String charset;
    //初始化alipayClient用到的参数:该公钥为测试账号公钥,开发者必须设置自己的公钥 ,否则会存在安全隐患
    @Value("${alipayPublicKey}")
    private String alipayPublicKey;
    //初始化alipayClient用到的参数:签名类型
    @Value("${signType}")
    private String signType;

    @Autowired
    private ShopStationMapper shopStationMapper;

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String upload() {
        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL, appId, privateKey, format, charset, alipayPublicKey, signType);
        ZhimaMerchantBorrowEntityUploadRequest request = new ZhimaMerchantBorrowEntityUploadRequest();

        ShopStation shopStation = shopStationMapper.findShopStationById(1L);

        //信用借还的签约产品码,传入固定值:	w1010100000000002858
        String productCode = GlobalConfig.Z_PRODUCT_CODE;
        /*
        类目Code，传入芝麻借还规定的类目Code，其他值会认为非法参数，参数值如下：
        雨伞：umbrella
        充电宝：power_bank
         */
        //      !!!     在接口测试联调过程中，接口入参【类目Code（category_code）】字段必须传【test】，
        // 如一旦传入其他类目Code值，会将测试数据直接上线，从而暴露给所有C端用户，将会给商户品牌带来极差影响。
        String categoryCode = "test";
        //外部实体编号，唯一标识一个实体，如自行车编号，机柜编号
        //注：商户维度下，类目Code（categoryCode）+实体编号（entity_code）唯一，
        // 一个商户下相同类目code+实体编号多次调用，将按照上传时间（upload_time）更新，
        // 更新规则取最新的upload_time快照数据
//        String entityCode = shopStation.getId().toString();
        String entityCode = "10";
        //地址位置经度，取值范围：经度-180~180，中国地区经度范围：73.66~135.05，示例：83.66
        String longitude = shopStation.getLongitude();
        //地址位置纬度，取值范围：纬度-90~90，中国地区经度范围：纬度3.86~53.55	，示例：5.87
        String latitude = shopStation.getLatitude();
        //实体名称，借用实体的描述，如XX雨伞，XX充电宝，XX自行车,例如：爱心雨伞
        String entityName = "共享充电宝";
        //地址描述,例如：杭州市西湖区文三路478号
        String addressDesc = shopStation.getAddress();
        //营业时间，格式：xx:xx-xx:xx，24小时制，如果是昼夜00:00—24:00	09:00—22:00
        String officeHoursDesc = shopStation.getStime() + "-" + shopStation.getEtime();
        //联系电话，手机11位数字，座机：区号－数字	0571-26888888
        String contactNumber = shopStation.getPhone();
        //是否收租金，Y:收租金，N:不收租金
        String collectRent = "Y";
        //可选,租金描述，该借还点的租金描述，例如：5元/小时，5-10元／小时	5元/小时
        String rentDesc = "";
        //是否可借用，Y:可借，N:不可借。如果不可借用，则不在芝麻借还频道地图展示
        String canBorrow = "Y";
        //可借用数量，如果是借用实物，如自行车，传1即可。如果是借用门店或借还机柜，则传入可借用的物品数量
        String canBorrowCnt = "1";
        //借用总数，如果是借用实物，如自行车，传1即可。如果是借用门店或借还机柜，则传入提供借还物品的总量
        String totalBorrowCnt = "1";
        //实体上传时间，某一借还实体信息多次上传，以最新上传时间数据为当前最新快照，格式：yyyy-mm-dd hh:MM:ss	2017-01-01 15:34:38
        String uploadTime = new SimpleDateFormat("YYYY-MM-dd HH:MM:ss").format(new Date());

        request.setBizContent("{" +
                "\"product_code\":\"" + productCode + "\"," +
                "\"category_code\":\"" + categoryCode + "\"," +
                "\"entity_code\":\"" + entityCode + "\"," +
                "\"longitude\":\"" + longitude + "\"," +
                "\"latitude\":\"" + latitude + "\"," +
                "\"entity_name\":\"" + entityName + "\"," +
                "\"address_desc\":\"" + addressDesc + "\"," +
                "\"office_hours_desc\":\"" + officeHoursDesc + "\"," +
                "\"contact_number\":\"" + contactNumber + "\"," +
                "\"collect_rent\":\"" + collectRent + "\"," +
                "\"rent_desc\":\"" + rentDesc + "\"," +
                "\"can_borrow\":\"" + canBorrow + "\"," +
                "\"can_borrow_cnt\":\"" + canBorrowCnt + "\"," +
                "\"total_borrow_cnt\":\"" + totalBorrowCnt + "\"," +
                "\"upload_time\":\"" + uploadTime + "\"" +
                "  }");
        ZhimaMerchantBorrowEntityUploadResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功\n");
            System.out.println("上传" + response.getMsg());
        } else {
            System.out.println("调用失败");
        }
        return null;
    }

}
