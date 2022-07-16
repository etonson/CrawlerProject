package exe;

import java.io.IOException;

import org.json.JSONObject;

import api.common.PicToValue;

public class TETE {

	public static void main(String[] args) throws IOException {
		System.out.println("first step");
		PicToValue pic = new PicToValue();
		JSONObject obj = pic.getDecodedValue("D:\\javaLib\\selenium-java-3.10.0\\Firefox\\okvat.png");
		System.out.println(obj.toString());
	}

}
