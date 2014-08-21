
public class AnemometerNRG40 implements Anemometer {
	final double anemo_m=0.765;
	final double anemo_b=0.35;
	
	public double pulsesToMS(int pulseCount, int seconds) {
		if ( pulseCount > 0 ) {
			return anemo_m*((double) pulseCount / (double) seconds) + anemo_b;
		}
		return 0.0;
	}

	public double tToMS(double t) {
		double f;
		
		if ( 0.0 == t ) {
			return 0.0;
		}
		
		f = 1.0 / t;
		
		return anemo_m * f + anemo_b;
	}

}
