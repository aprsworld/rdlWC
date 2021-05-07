import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TalkerId;


public interface NMEASentencePSAT extends Sentence {

	public String getSubsentenceName();
	
	public boolean isHPR();
	public String getHPRTime();
	public Double getHPRHeading();
	public Double getHPRPitch();
	public Double getHPRRoll();
	public String getHPRType();

}
