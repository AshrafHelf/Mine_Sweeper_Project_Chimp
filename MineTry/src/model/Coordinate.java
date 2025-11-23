package model;


import java.util.Objects;


public record Coordinate(int col, int row) {
public Coordinate { if (col < 0 || row < 0) throw new IllegalArgumentException("neg index"); }
@Override public String toString() { return col + "," + row; }
@Override public boolean equals(Object o) { return o instanceof Coordinate c && c.col==col && c.row==row; }
@Override public int hashCode() { return Objects.hash(col, row); }
}