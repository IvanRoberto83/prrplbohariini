package com.rplbo.datamhsfx.datamahasiswa;

public class Mahasiswa {
    private String nim;
    private String nama;
    private double nilai;
    private String foto;

    public Mahasiswa(String nim, String nama, double nilai, String foto) {
        this.nim = nim;
        this.nama = nama;
        this.nilai = nilai;
        this.foto = foto;
    }

    public String getNim() {
        return nim;
    }

    public void setNim(String nim) {
        this.nim = nim;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public double getNilai() {
        return nilai;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public void setNilai(double nilai) {
        this.nilai = nilai;
    }

    public String toString(){
        StringBuilder hasil = new StringBuilder();
        hasil.append(nim+" - "+nama+" ("+nilai+"), foto: "+foto);
        return hasil.toString();
    }
}
