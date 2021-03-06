package lottery.domains.content.payment.htf;

import admin.web.WebJSONObject;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javautils.http.HttpClientUtil;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lottery.domains.content.AbstractPayment;
import lottery.domains.content.entity.PaymentChannel;
import lottery.domains.content.entity.PaymentChannelBank;
import lottery.domains.content.entity.UserCard;
import lottery.domains.content.entity.UserWithdraw;
import lottery.domains.content.payment.lepay.utils.StringUtil;
import lottery.domains.content.payment.utils.MoneyFormat;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Component
public class HTFPayment
  extends AbstractPayment
  implements InitializingBean
{
  private static final String INPUT_CHARSET = "UTF-8";
  private static final String REMARK = "df";
  private static final String VERSION = "2";
  private static final String NOTIFY_URL = "http://www.yyy.com";
  private static final String DEFAULT_PROVICE = "北京市";
  private static final String DEFAULT_CITY = "北京市";
  private static Map<Integer, String> BANK_CODES = new HashMap();
  private static Map<Integer, String> BRANCH_NAMES = new HashMap();
  @Value("${htf.daifu.url}")
  private String daifuUrl;
  @Value("${htf.daifu.queryurl}")
  private String queryUrl;
  
  public void afterPropertiesSet()
    throws Exception
  {
    BRANCH_NAMES.put(Integer.valueOf(1), "中国工商银行股份有限公司上海市龙江路支行");
    BRANCH_NAMES.put(Integer.valueOf(2), "中国建设银行北京市分行营业部");
    BRANCH_NAMES.put(Integer.valueOf(3), "中国农业银行股份有限公司忻州和平分理处");
    BRANCH_NAMES.put(Integer.valueOf(4), "招商银行股份有限公司厦门金湖支行");
    BRANCH_NAMES.put(Integer.valueOf(5), "中国银行股份有限公司赣州市客家大道支行");
    BRANCH_NAMES.put(Integer.valueOf(6), "交通银行北京安翔里支行");
    BRANCH_NAMES.put(Integer.valueOf(7), "上海浦东发展银行安亭支行");
    BRANCH_NAMES.put(Integer.valueOf(8), "兴业银行北京安华支行");
    BRANCH_NAMES.put(Integer.valueOf(9), "中信银行北京安贞支行");
    BRANCH_NAMES.put(Integer.valueOf(10), "宁波银行股份有限公司北京东城支行");
    BRANCH_NAMES.put(Integer.valueOf(11), "上海银行股份有限公司北京安贞支行");
    BRANCH_NAMES.put(Integer.valueOf(12), "杭州银行股份有限公司上海北新泾支行");
    BRANCH_NAMES.put(Integer.valueOf(13), "渤海银行股份有限公司北京朝阳门支行");
    BRANCH_NAMES.put(Integer.valueOf(14), "浙商银行股份有限公司杭州滨江支行");
    BRANCH_NAMES.put(Integer.valueOf(15), "广发银行股份有限公司北京朝阳北路支行");
    BRANCH_NAMES.put(Integer.valueOf(16), "中国邮政储蓄银行股份有限公司北京昌平区北七家支行");
    BRANCH_NAMES.put(Integer.valueOf(17), "深圳发展银行");
    BRANCH_NAMES.put(Integer.valueOf(18), "中国民生银行股份有限公北京西大望路支行");
    BRANCH_NAMES.put(Integer.valueOf(19), "中国光大银行股份有限公司北京安定门支行");
    BRANCH_NAMES.put(Integer.valueOf(20), "华夏银行北京德外支行");
    BRANCH_NAMES.put(Integer.valueOf(21), "北京银行安定门支行");
    BRANCH_NAMES.put(Integer.valueOf(22), "南京银行股份有限公司北京车公庄支行");
    BRANCH_NAMES.put(Integer.valueOf(23), "平安银行股份有限公司北京北苑支行");
    BRANCH_NAMES.put(Integer.valueOf(24), "北京农村商业银行股份有限公司漷县支行");
    
    BANK_CODES.put(Integer.valueOf(1), "1");
    BANK_CODES.put(Integer.valueOf(2), "2");
    BANK_CODES.put(Integer.valueOf(3), "3");
    BANK_CODES.put(Integer.valueOf(4), "7");
    BANK_CODES.put(Integer.valueOf(5), "5");
    BANK_CODES.put(Integer.valueOf(6), "6");
    BANK_CODES.put(Integer.valueOf(7), "9");
    BANK_CODES.put(Integer.valueOf(8), "13");
    BANK_CODES.put(Integer.valueOf(9), "12");
    BANK_CODES.put(Integer.valueOf(10), "17");
    BANK_CODES.put(Integer.valueOf(11), "16");
    BANK_CODES.put(Integer.valueOf(12), "15");
    BANK_CODES.put(Integer.valueOf(15), "11");
    BANK_CODES.put(Integer.valueOf(16), "4");
    BANK_CODES.put(Integer.valueOf(18), "14");
    BANK_CODES.put(Integer.valueOf(19), "8");
    BANK_CODES.put(Integer.valueOf(20), "10");
    BANK_CODES.put(Integer.valueOf(23), "18");
  }
  
  public String daifu(WebJSONObject json, UserWithdraw order, UserCard card, PaymentChannelBank bank, PaymentChannel channel)
  {
    try
    {
      logStart(order, bank, channel);
      return daifuInternal(json, order, card, bank, channel);
    }
    catch (Exception e)
    {
      logException(order, bank, channel, "代付请求失败", e);
      json.set(Integer.valueOf(2), "2-4000");
    }
    return null;
  }
  
  private String daifuInternal(WebJSONObject json, UserWithdraw order, UserCard card, PaymentChannelBank bank, PaymentChannel channel)
  {
    try
    {
      String bankCode = (String)BANK_CODES.get(Integer.valueOf(card.getBankId()));
      if (StringUtil.isEmpty(bankCode))
      {
        json.set(Integer.valueOf(2), "2-4012");
        return null;
      }
      String branchName = (String)BRANCH_NAMES.get(Integer.valueOf(card.getBankId()));
      if (StringUtil.isEmpty(branchName))
      {
        json.set(Integer.valueOf(2), "2-4012");
        return null;
      }
      String amount = MoneyFormat.moneyToYuanForPositive(order.getRecMoney()+"");
      String version = "2";
      String agent_id = channel.getMerCode();
      String batch_no = order.getBillno();
      String batch_amt = amount;
      String batch_num = "1";
      String notify_url = "http://www.yyy.com";
      String ext_param1 = order.getBillno();
      
      Map<String, String> paramsMap = new TreeMap();
      paramsMap.put("version", version);
      paramsMap.put("agent_id", agent_id);
      paramsMap.put("batch_no", batch_no);
      paramsMap.put("batch_amt", batch_amt);
      paramsMap.put("batch_num", batch_num);
      
      String detail_data = "%s^%s^%s^%s^%s^%s^%s^%s^%s^%s";
      detail_data = String.format(detail_data, new Object[] { order.getBillno(), bankCode, "0", order.getCardId(), order.getCardName(), amount, "df", "北京市", "北京市", branchName });
      
      paramsMap.put("detail_data", detail_data);
      paramsMap.put("notify_url", notify_url);
      paramsMap.put("ext_param1", ext_param1);
      
      String signStr = "agent_id=%s&batch_amt=%s&batch_no=%s&batch_num=%s&detail_data=%s&ext_param1=%s&key=%s&notify_url=%s&version=%s";
      signStr = String.format(signStr, new Object[] { agent_id, batch_amt, batch_no, batch_num, detail_data, ext_param1, channel.getExt1(), notify_url, version });
      signStr = signStr.toLowerCase();
      
      String sign = DigestUtils.md5Hex(signStr).toLowerCase();
      paramsMap.put("sign", sign);
      
      String url = this.daifuUrl + "?_=" + System.currentTimeMillis();
      
      String retStr = HttpClientUtil.post(url, paramsMap, null, 60000);
      if (StringUtils.isEmpty(retStr))
      {
        logError(order, bank, channel, "接口返回空，可能是请求超时");
        json.set(Integer.valueOf(-1), "-1");
        return null;
      }
      HTFPayResult result = transferPayResult(order, channel, retStr);
      if (result == null)
      {
        logError(order, bank, channel, "请求失败，解析返回数据失败，返回数据为：" + retStr);
        json.setWithParams(Integer.valueOf(2), "2-4007", new Object[] { StringUtils.abbreviate(retStr, 20) });
        return null;
      }
      if (isAcceptedRequest(result.getRetCode()))
      {
        logSuccess(order, order.getBillno(), channel);
        return order.getBillno();
      }
      if (StringUtils.isEmpty(result.getRetMsg()))
      {
        String msg = "未知错误";
        logError(order, bank, channel, "请求返回空的错误消息，返回数据：" + retStr + "，开始查询订单状态");
        
        HTFPayQueryResult queryResult = query(order, channel);
        if ((isAcceptedRequest(queryResult.getRetCode())) && 
          (StringUtils.isNotEmpty(queryResult.getHyBillNo())))
        {
          logSuccess(order, queryResult.getHyBillNo(), channel);
          return queryResult.getHyBillNo();
        }
        logError(order, bank, channel, "请求失败，返回数据为：" + retStr);
        json.setWithParams(Integer.valueOf(2), "2-4002", new Object[] { msg });
        return null;
      }
      logError(order, bank, channel, "请求失败，返回数据为：" + retStr);
      json.setWithParams(Integer.valueOf(2), "2-4002", new Object[] { result.getRetMsg() });
      return null;
    }
    catch (Exception e)
    {
      logException(order, bank, channel, "代付请求失败", e);
      json.set(Integer.valueOf(-1), "-1");
    }
    return null;
  }
  
  public HTFPayQueryResult query(UserWithdraw order, PaymentChannel channel)
  {
    try
    {
      Map<String, String> paramsMap = new TreeMap();
      String version = "2";
      String agent_id = channel.getMerCode();
      String batch_no = order.getBillno();
      
      paramsMap.put("version", version);
      paramsMap.put("agent_id", agent_id);
      paramsMap.put("batch_no", batch_no);
      
      String signStr = "agent_id=%s&batch_no=%s&key=%s&version=%s";
      signStr = String.format(signStr, new Object[] { agent_id, batch_no, channel.getExt1(), version });
      signStr = signStr.toLowerCase();
      
      String sign = DigestUtils.md5Hex(signStr).toLowerCase();
      
      paramsMap.put("sign", sign);
      
      String url = this.queryUrl + "?_=" + System.currentTimeMillis();
      
      String retStr = HttpClientUtil.post(url, paramsMap, null, 60000);
      if (StringUtils.isEmpty(retStr))
      {
        logError(order, null, channel, "查询请求失败，发送请求后返回空数据");
        return null;
      }
      logInfo(order, null, channel, "查询返回数据：" + retStr);
      
      HTFPayQueryResult queryResult = transferPayQueryResult(order, channel, retStr);
      if (queryResult == null)
      {
        logError(order, null, channel, "查询请求失败，解析返回数据失败");
        return null;
      }
      return queryResult;
    }
    catch (Exception e)
    {
      logException(order, null, channel, "查询请求失败", e);
    }
    return null;
  }
  
  public boolean isAcceptedRequest(String retCode)
  {
    return "0000".equalsIgnoreCase(retCode);
  }
  
  public int transferBankStatus(String detailData)
  {
    String[] datas = detailData.split("\\^");
    if ((datas == null) || (datas.length < 5)) {
      return -4;
    }
    String bankStatus = datas[4];
    if ("S".equalsIgnoreCase(bankStatus)) {
      return 2;
    }
    if ("F".equalsIgnoreCase(bankStatus)) {
      return -5;
    }
    if ("P".equalsIgnoreCase(bankStatus)) {
      return 1;
    }
    return -4;
  }
  
  private HTFPayResult transferPayResult(UserWithdraw order, PaymentChannel channel, String xml)
  {
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(new InputSource(new StringReader(xml)));
      
      Node rootChild = doc.getFirstChild();
      if (rootChild == null) {
        return null;
      }
      HTFPayResult result = new HTFPayResult();
      
      NodeList childNodes = rootChild.getChildNodes();
      int length = childNodes.getLength();
      for (int i = 0; i < length; i++)
      {
        Node item = childNodes.item(i);
        String nodeName = item.getNodeName();
        String textContent = item.getTextContent();
        if ("ret_code".equalsIgnoreCase(nodeName)) {
          result.setRetCode(textContent);
        } else if ("ret_msg".equalsIgnoreCase(nodeName)) {
          result.setRetMsg(textContent);
        }
      }
      return result;
    }
    catch (Exception e)
    {
      logException(order, null, channel, "转换支付结果XML异常", e);
    }
    return null;
  }
  
  private HTFPayQueryResult transferPayQueryResult(UserWithdraw order, PaymentChannel channel, String xml)
  {
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(new InputSource(new StringReader(xml)));
      
      Node rootChild = doc.getFirstChild();
      if (rootChild == null) {
        return null;
      }
      HTFPayQueryResult queryResult = new HTFPayQueryResult();
      
      NodeList childNodes = rootChild.getChildNodes();
      int length = childNodes.getLength();
      for (int i = 0; i < length; i++)
      {
        Node item = childNodes.item(i);
        String nodeName = item.getNodeName();
        String textContent = item.getTextContent();
        if ("ret_code".equalsIgnoreCase(nodeName)) {
          queryResult.setRetCode(textContent);
        } else if ("ret_msg".equalsIgnoreCase(nodeName)) {
          queryResult.setRetMsg(textContent);
        } else if ("agent_id".equalsIgnoreCase(nodeName)) {
          queryResult.setAgentId(textContent);
        } else if ("hy_bill_no".equalsIgnoreCase(nodeName)) {
          queryResult.setHyBillNo(textContent);
        } else if ("batch_no".equalsIgnoreCase(nodeName)) {
          queryResult.setBatchNo(textContent);
        } else if ("batch_amt".equalsIgnoreCase(nodeName)) {
          queryResult.setBatchAmt(textContent);
        } else if ("batch_num".equalsIgnoreCase(nodeName)) {
          queryResult.setBatchNum(textContent);
        } else if ("detail_data".equalsIgnoreCase(nodeName)) {
          queryResult.setDetailData(textContent);
        } else if ("ext_param1".equalsIgnoreCase(nodeName)) {
          queryResult.setExtParam1(textContent);
        } else if ("sign".equalsIgnoreCase(nodeName)) {
          queryResult.setSign(textContent);
        }
      }
      return queryResult;
    }
    catch (Exception e)
    {
      logException(order, null, channel, "转换支付结果XML异常", e);
    }
    return null;
  }
  
  public static void main(String[] args) {}
}
