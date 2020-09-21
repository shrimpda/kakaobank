package com.kakaobank.common.unit;

import com.kakaobank.common.io.StreamOutput;
import com.kakaobank.common.io.Writeable;
import com.kakaobank.common.util.StringUtil;
import java.io.IOException;
import org.bouncycastle.util.Strings;

public class SizeValue implements Writeable, Comparable<SizeValue> {

	private final long size;
	private final SizeUnit sizeUnit;

	public SizeValue(long singles) {
		this(singles, SizeUnit.SINGLE);
	}

	public SizeValue(long size, SizeUnit sizeUnit) {
		this.size = size;
		this.sizeUnit = sizeUnit;
	}

	@Override
	public void writeTo(StreamOutput streamOutput) throws IOException {
		streamOutput.writeLong(size);
	}

	public long singles() {
		return sizeUnit.toSingles(size);
	}

	public long getSingles() {
		return singles();
	}

	public long kilo() {
		return sizeUnit.toKilo(size);
	}

	public long getKilo() {
		return kilo();
	}

	public long mega() {
		return sizeUnit.toMega(size);
	}

	public long getMega() {
		return mega();
	}

	public long giga() {
		return sizeUnit.toGiga(size);
	}

	public long getGiga() {
		return giga();
	}

	public long tera() {
		return sizeUnit.toTera(size);
	}

	public long getTera() {
		return tera();
	}

	public long peta() {
		return sizeUnit.toPeta(size);
	}

	public long getPeta() {
		return peta();
	}

	public double kiloFrac() {
		return ((double) singles()) / SizeUnit.C1;
	}

	public double getKiloFrac() {
		return kiloFrac();
	}

	public double megaFrac() {
		return ((double) singles()) / SizeUnit.C2;
	}

	public double getMegaFrac() {
		return megaFrac();
	}

	public double gigaFrac() {
		return ((double) singles()) / SizeUnit.C3;
	}

	public double getGigaFrac() {
		return gigaFrac();
	}

	public double teraFrac() {
		return ((double) singles()) / SizeUnit.C4;
	}

	public double getTeraFrac() {
		return teraFrac();
	}

	public double petaFrac() {
		return ((double) singles()) / SizeUnit.C5;
	}

	public double getPetaFrac() {
		return petaFrac();
	}

	@Override
	public String toString() {
		long singles = singles();
		double value = singles;
		String suffix = "";
		if (singles >= SizeUnit.C5) {
			value = petaFrac();
			suffix = "p";
		} else if (singles >= SizeUnit.C4) {
			value = teraFrac();
			suffix = "t";
		} else if (singles >= SizeUnit.C3) {
			value = gigaFrac();
			suffix = "g";
		} else if (singles >= SizeUnit.C2) {
			value = megaFrac();
			suffix = "m";
		} else if (singles >= SizeUnit.C1) {
			value = kiloFrac();
			suffix = "k";
		}

		return StringUtil.formatDecimals(value, suffix);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		return compareTo((SizeValue) o) == 0;
	}

	@Override
	public int hashCode() {
		return Double.hashCode(((double) size) * sizeUnit.toSingles(1));
	}

	@Override
	public int compareTo(SizeValue other) {
		double thisValue = ((double) size) * sizeUnit.toSingles(1);
		double otherValue = ((double) other.size) * other.sizeUnit.toSingles(1);
		return Double.compare(thisValue, otherValue);
	}
}
