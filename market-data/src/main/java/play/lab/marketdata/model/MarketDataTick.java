package play.lab.marketdata.model;

public class MarketDataTick {
    private String pair;
    private double mid, bid, ask;
    private long timestamp;

    public MarketDataTick(String pair, double mid, double bid, double ask, long timestamp) {
        this.pair = pair;
        this.mid = mid;
        this.bid = bid;
        this.ask = ask;
        this.timestamp = timestamp;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public double getMid() {
        return mid;
    }

    public void setMid(double mid) {
        this.mid = mid;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public double getAsk() {
        return ask;
    }

    public void setAsk(double ask) {
        this.ask = ask;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
