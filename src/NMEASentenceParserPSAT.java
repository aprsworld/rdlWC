import net.sf.marineapi.nmea.parser.*;
import net.sf.marineapi.nmea.sentence.*;


public class NMEASentenceParserPSAT extends SentenceParser implements NMEASentencePSAT {

	public NMEASentenceParserPSAT(String s) {
		super(s, "SAT");
	}
	
	public NMEASentenceParserPSAT(TalkerId tid) {
		super(tid,"SAT",6);
	}



	public String getSubsentenceName() {
		return getStringValue(0);
	}

	
	public boolean isHPR() {
		if ( getSubsentenceName().compareTo("HPR")==0 )
			return true;
		
		return false;
	}
	
	public String getHPRTime() {
		if ( ! isHPR() )
			return null;
		
		return getStringValue(1);
	}

	public Double getHPRHeading() {
		if ( ! isHPR() )
			return null;
		
		return Double.parseDouble(getStringValue(2));
	}

	public Double getHPRPitch() {
		if ( ! isHPR() )
			return null;
		
		return Double.parseDouble(getStringValue(3));	
	}

	public Double getHPRRoll() {
		if ( ! isHPR() )
			return null;
		
		return Double.parseDouble(getStringValue(4));	
	}

	public String getHPRType() {
		if ( ! isHPR() )
			return null;
		
		return getStringValue(5);	
	}
	
	
}

