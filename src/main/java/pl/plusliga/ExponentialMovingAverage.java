package pl.plusliga;

import java.util.ArrayDeque;
import java.util.Deque;

public class ExponentialMovingAverage
    extends org.springframework.integration.support.management.ExponentialMovingAverage {

  protected final int window;
  protected final MetricType type;
  protected final Deque<Double> samples = new ArrayDeque<Double>();

  public ExponentialMovingAverage(int window, MetricType type) {
    super(window);
    this.window = window;
    this.type = type;
  }

  @Override
  public synchronized void reset() {
    super.reset();
    samples.clear();
  }

  @Override
  public synchronized void append(double value) {
    super.append(value);
    samples.add(value);
    if (samples.size() > window) {
      samples.removeFirst();
    }
  }

  public double getLastValue() {
    return samples.size() > 0 ? samples.peekLast() : 0.0;
  }

  @Override
  public double getMax() {
    return samples.stream().max(Double::compare).orElse(0.0);
  }

  @Override
  public double getMin() {
    return samples.stream().min(Double::compare).orElse(0.0);
  }

  @Override
  public String toString() {
    return String.format(type.getFormat(), getMin(), getMean(), getMax(), getLastValue());
  }

}
