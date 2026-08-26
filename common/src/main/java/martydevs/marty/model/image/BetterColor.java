package martydevs.marty.model.image;

public enum BetterColor {

    CIE76_D65(Tristimulus.D65),
    CIE76_D50(Tristimulus.D50),
    CIEDE2000_D65(Tristimulus.D65),
    CIEDE2000_D50(Tristimulus.D50);

    public final Tristimulus tristimulus;

    BetterColor(Tristimulus tristimulus) {
        this.tristimulus = tristimulus;
    }

    public enum Tristimulus {

        D65(95.046f, 100, 108.883f),
        D50(96.42f, 100, 82.49f);

        public final float[] xyz;

        Tristimulus(float... xyz) {
            this.xyz = xyz;
        }

    }

}
