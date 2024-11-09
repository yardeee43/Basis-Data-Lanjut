/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.utsBdl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author bachtiar
 */
public class Pemesanan extends javax.swing.JFrame {

    Connection conn;

    private final String[] columns = {"ID pelanggan", "Barang", "Qty", "Total harga"};
    private final ArrayList<String[]> dataList = new ArrayList<>();
    DefaultTableModel model = new DefaultTableModel();
    String driver = "org.postgresql.Driver";
    String koneksi = "jdbc:postgresql://localhost:5432/Dollah";
    String user = "postgres";
    String password = "yarde";
    int akhir;
    private String statusPembayaran;

    private String generateNewId(Connection conn) throws SQLException {
        String newId = "PAY0001";  // ID awal jika belum ada data

        // Query untuk mendapatkan ID pemesanan terakhir
        String sql = "SELECT id_pemesanan FROM pemesanan ORDER BY id_pemesanan DESC LIMIT 1";
        try (PreparedStatement statement = conn.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                String lastId = resultSet.getString("id_pemesanan");
                System.out.println("ID terakhir dari database: '" + lastId + "'");  // Logging ID terakhir

                if (lastId != null && lastId.length() >= 4) {
                    // Hapus spasi sebelum parsing
                    lastId = lastId.trim(); // Menghapus spasi di awal dan akhir
                    try {
                        int lastIdNumber = Integer.parseInt(lastId.substring(3));  // Ambil angka dari ID terakhir
                        lastIdNumber++;  // Increment ID
                        newId = "PAY" + String.format("%04d", lastIdNumber); // Format ke ID baru
                    } catch (NumberFormatException e) {
                        System.out.println("Format ID tidak valid: " + lastId);
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("ID terakhir tidak valid: " + lastId);
                }
            } else {
                System.out.println("Tidak ada ID pemesanan yang ditemukan, menggunakan ID default.");
            }

        }

        return newId;
    }

    public String idbarang(String a) {
        String id = null;
        try {
            String sql = "select id_barang from barang where nama_barang = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, a);
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                id = res.getString("id_barang");

            }
        } catch (Exception e) {
        }
        return id;
    }

    public void updateTotalLabel() {
        int totalSemua = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            String totalStr = model.getValueAt(i, 3).toString();

            totalSemua += Integer.parseInt(totalStr);
        }

        totalKeseluruhan.setText(String.valueOf(totalSemua));
    }

    private void setupListeners() {
        qtyTxt.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateTotal();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateTotal();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateTotal();
            }
        });
    }

    private void updateTotal() {

        try {
            int qty = Integer.parseInt(qtyTxt.getText());
            int harga = Integer.parseInt(TotalLbl.getText());

            long total = qty * harga;

            TotalLbl.setText(String.valueOf(total));

        } catch (NumberFormatException e) {
            int harg = Integer.parseInt(tampilHarga());
            TotalLbl.setText(String.valueOf(harg));

        }
    }

    public final void refreshModel() {
        model.setColumnIdentifiers(columns);
        for (String[] data : dataList) {
            model.addRow(data);
        }
        table.setModel(model);
    }

    public void konek() {
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(koneksi, user, password);

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void tampilId() {
        try {
            String id = "Select id_pelanggan from pelanggan order by id_pelanggan asc ";
            konek();
            PreparedStatement ps = conn.prepareStatement(id);
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                String i = res.getString("id_pelanggan");
                idCbox.addItem(i);
            }
        } catch (Exception e) {
        }
    }

    public void tampilJenis() {
        konek();
        jenisCb.removeAllItems();
        try {
            String sql = "select distinct jenis_barang from barang";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                String jenis = res.getString("jenis_barang");
                jenisCb.addItem(jenis);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void tampilStok() {
        try {
            konek();
            String sql = "select qty from barang where nama_barang = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, namaBarangCbox.getSelectedItem().toString());
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                String qty = res.getString("qty");
                stokLbl.setText(qty);
            }
        } catch (Exception e) {
        }
    }

    public void tampilnama() {
        konek();
        namaBarangCbox.removeAllItems();

        try {
            String sql = "SELECT nama_barang FROM barang WHERE jenis_barang = ?";
            PreparedStatement ps = conn.prepareStatement(sql);

            if (jenisCb != null && jenisCb.getSelectedItem() != null) {
                ps.setString(1, jenisCb.getSelectedItem().toString());
            } else {
                System.out.println("Jenis barang tidak dipilih.");
                return;
            }

            ResultSet res = ps.executeQuery();
            while (res.next()) {
                String jenis = res.getString("nama_barang");
                namaBarangCbox.addItem(jenis);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String tampilHarga() {
        String harga = null;
        try {
            konek();
            String sql = "select harga from barang where nama_barang = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, namaBarangCbox.getSelectedItem().toString());
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                harga = res.getString("harga");
                TotalLbl.setText(harga);
            }

        } catch (Exception e) {
        }
        return harga;
    }

    /**
     * Creates new form P
     */
    public Pemesanan() {

        initComponents();
        this.refreshModel();
        konek();
        tampilId();
        tampilJenis();
        tampilHarga();
//        tampilnama();
        setupListeners();
//        updateTotal();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        idCbox = new javax.swing.JComboBox<>();
        namaLbl = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jenisCb = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        namaBarangCbox = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        TotalLbl = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        insertBtn = new javax.swing.JButton();
        deleteBtn = new javax.swing.JButton();
        clearBtn = new javax.swing.JButton();
        sumbitBTn = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        stokLbl = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        qtyTxt = new javax.swing.JTextField();
        totalKeseluruhan = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        idCbox.setFont(new java.awt.Font("Sylfaen", 1, 14)); // NOI18N
        idCbox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "SIilahkan Pilih ID" }));
        idCbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                idCboxActionPerformed(evt);
            }
        });

        namaLbl.setFont(new java.awt.Font("Sylfaen", 1, 14)); // NOI18N
        namaLbl.setText("-");
        namaLbl.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                namaLblAncestorAdded(evt);
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        jLabel1.setFont(new java.awt.Font("Sylfaen", 1, 14)); // NOI18N
        jLabel1.setText("Select ID");

        jLabel2.setFont(new java.awt.Font("Sylfaen", 1, 14)); // NOI18N
        jLabel2.setText("Nama");

        jLabel4.setFont(new java.awt.Font("Sylfaen", 1, 14)); // NOI18N
        jLabel4.setText("Jenis");

        jenisCb.setFont(new java.awt.Font("Sylfaen", 1, 14)); // NOI18N
        jenisCb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jenisCbActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Sylfaen", 1, 14)); // NOI18N
        jLabel5.setText("Nama Barang");

        namaBarangCbox.setFont(new java.awt.Font("Sylfaen", 1, 14)); // NOI18N
        namaBarangCbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                namaBarangCboxActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Sylfaen", 1, 14)); // NOI18N
        jLabel3.setText("Qty");

        jLabel6.setFont(new java.awt.Font("Sylfaen", 1, 14)); // NOI18N
        jLabel6.setText("Harga  :");

        TotalLbl.setFont(new java.awt.Font("Sylfaen", 1, 14)); // NOI18N
        TotalLbl.setText("-");

        table.setFont(new java.awt.Font("Sylfaen", 1, 12)); // NOI18N
        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(table);

        insertBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        insertBtn.setText("Insert");
        insertBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertBtnActionPerformed(evt);
            }
        });

        deleteBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        deleteBtn.setText("Delete");
        deleteBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteBtnActionPerformed(evt);
            }
        });

        clearBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        clearBtn.setText("Clear");
        clearBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearBtnActionPerformed(evt);
            }
        });

        sumbitBTn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        sumbitBTn.setText("Submit");
        sumbitBTn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sumbitBTnActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("MS Gothic", 1, 24)); // NOI18N
        jLabel7.setText("ME & U");

        jButton1.setText("Back");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setText("Stok : ");

        stokLbl.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        stokLbl.setText("-");

        jButton2.setText("Next");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        qtyTxt.setText("1");
        qtyTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qtyTxtActionPerformed(evt);
            }
        });

        totalKeseluruhan.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        totalKeseluruhan.setText("-");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel9.setText("Total Yang harus Dibayar");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addGap(52, 52, 52)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(namaLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(namaBarangCbox, 0, 210, Short.MAX_VALUE)
                            .addComponent(idCbox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jenisCb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(12, 12, 12)
                        .addComponent(jLabel8)
                        .addGap(18, 18, 18)
                        .addComponent(stokLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(52, 52, 52)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(insertBtn)
                                .addGap(18, 18, 18)
                                .addComponent(deleteBtn)
                                .addGap(18, 18, 18)
                                .addComponent(clearBtn))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(qtyTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel6)
                                .addGap(18, 18, 18)
                                .addComponent(TotalLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 9, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(235, 235, 235))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(sumbitBTn, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addGap(47, 47, 47)
                                .addComponent(totalKeseluruhan, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(15, 15, 15))))
            .addGroup(layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel7))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1)
                        .addComponent(jButton2)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(31, 31, 31)
                        .addComponent(jLabel2))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(idCbox, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(namaLbl)
                        .addGap(6, 6, 6)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jenisCb, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(namaBarangCbox, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(stokLbl)
                    .addComponent(jLabel5))
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(qtyTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(TotalLbl)
                    .addComponent(jLabel6))
                .addGap(36, 36, 36)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(insertBtn)
                    .addComponent(deleteBtn)
                    .addComponent(clearBtn))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalKeseluruhan)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(sumbitBTn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void idCboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_idCboxActionPerformed
        // TODO add your handling code here:
        try {
            konek();
            String sql = "select nama from pelanggan where id_pelanggan = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, idCbox.getSelectedItem().toString());
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                String nama = res.getString("nama");
                namaLbl.setText(nama);
            }
        } catch (Exception e) {
        }
    }//GEN-LAST:event_idCboxActionPerformed

    private void jenisCbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jenisCbActionPerformed
        // TODO add your handling code here:
        tampilnama();

    }//GEN-LAST:event_jenisCbActionPerformed

    private void namaBarangCboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_namaBarangCboxActionPerformed
        // TODO add your handling code here:
        tampilHarga();
        tampilStok();
    }//GEN-LAST:event_namaBarangCboxActionPerformed

    private void insertBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertBtnActionPerformed

        if (idCbox.getSelectedItem().equals("SIilahkan Pilih ID")) {
            JOptionPane.showMessageDialog(this, "milih id dulu bos");
        } else if (qtyTxt.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "lupa masukin kuantitas ya wkwk");
        } else {
            try {

                int stok = Integer.parseInt(stokLbl.getText());
                int kuantitas = Integer.parseInt(qtyTxt.getText());

                if (kuantitas > stok) {
                    JOptionPane.showMessageDialog(this, "Stok ga cukup");
                    qtyTxt.setText("");
                } else if (kuantitas <= 0) {
                    JOptionPane.showMessageDialog(this, "masukkin kuantitas yang bener jir");
                    qtyTxt.setText("");
                } else {

                    String id = idCbox.getSelectedItem().toString();
                    String barang = namaBarangCbox.getSelectedItem().toString();
                    String qty = qtyTxt.getText();
                    String total = TotalLbl.getText();
                    final String[] row = new String[]{
                        id, barang, qty, total
                    };
                    updateTotal();
                    model.addRow(row);
                    qtyTxt.setText("");
                    updateTotalLabel();
                    int akhir = stok - kuantitas;
                    stokLbl.setText(String.valueOf(akhir));
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "masukkan angka jir bukan huruf");
                qtyTxt.setText("");
            }
        }

    }//GEN-LAST:event_insertBtnActionPerformed

    private void deleteBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteBtnActionPerformed
        // TODO add your handling code here:
        int selectedRowIndex = table.getSelectedRow();

        if (selectedRowIndex != -1) {
            // Hapus baris yang dipilih
            model.removeRow(selectedRowIndex);
            tampilStok();
        } else {
            // Jika tid ak ada baris yang dipilih, tampilkan pesan kesalahan
            JOptionPane.showMessageDialog(this, "Silakan pilih baris untuk dihapus", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_deleteBtnActionPerformed

    private void clearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBtnActionPerformed
        // TODO add your handling code here:
        model.setRowCount(0);
        tampilStok();
    }//GEN-LAST:event_clearBtnActionPerformed

    private void sumbitBTnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sumbitBTnActionPerformed
        // TODO add your handling code here:

        Pembayaran frame = new Pembayaran();
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "mau beli apa jir");
        } else {

            try {
                conn.setAutoCommit(false);
                String pesanan = "insert into pemesanan (id_pemesanan,id_pelanggan) values (?,?)";
                PreparedStatement ps = conn.prepareStatement(pesanan);
                String id = generateNewId(conn);
                ps.setString(1, id);
                ps.setString(2, idCbox.getSelectedItem().toString());
                ps.executeUpdate();

                String detail = "insert into detail_pemesanan_barang (id_pemesanan,id_barang,jumlah)values (?,?,?)";
                PreparedStatement ps1 = conn.prepareStatement(detail);

                for (int i = 0; i < model.getRowCount(); i++) {

                    String qty = (String) model.getValueAt(i, 2);
                    String namaBarang = (String) model.getValueAt(i, 1);

                    String idbarang = idbarang(namaBarang);
                    ps1.setString(1, id);
                    ps1.setString(2, idbarang);
                    ps1.setInt(3, Integer.parseInt(qty));
                    ps1.executeUpdate();

                    String update = "UPDATE barang SET qty = qty - ? WHERE nama_barang = ?";
                    PreparedStatement psi = conn.prepareStatement(update);
                    psi.setInt(1, Integer.parseInt(qty));
                    psi.setString(2, namaBarang);
                    psi.executeUpdate();

                }

                frame.lblId.setText(idCbox.getSelectedItem().toString());
                frame.namaLblp.setText(namaLbl.getText());
                frame.totalLbl.setText(totalKeseluruhan.getText());
                JOptionPane.showMessageDialog(this, "berhasil input");
                frame.setVisible(true);
                conn.commit();
                this.dispose();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }//GEN-LAST:event_sumbitBTnActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        this.dispose();
        Pelanggan frame = new Pelanggan();
        frame.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        this.dispose();
        Pembayaran frame = new Pembayaran();
        frame.setVisible(true);

    }//GEN-LAST:event_jButton2ActionPerformed

    private void namaLblAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_namaLblAncestorAdded
        // TODO add your handling code here:
    }//GEN-LAST:event_namaLblAncestorAdded

    private void qtyTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qtyTxtActionPerformed

        updateTotal();
    }//GEN-LAST:event_qtyTxtActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Pemesanan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Pemesanan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Pemesanan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Pemesanan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Pemesanan().setVisible(true);
            }
        });

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel TotalLbl;
    private javax.swing.JButton clearBtn;
    private javax.swing.JButton deleteBtn;
    private javax.swing.JComboBox<String> idCbox;
    private javax.swing.JButton insertBtn;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox<String> jenisCb;
    private javax.swing.JComboBox<String> namaBarangCbox;
    private javax.swing.JLabel namaLbl;
    private javax.swing.JTextField qtyTxt;
    private javax.swing.JLabel stokLbl;
    private javax.swing.JButton sumbitBTn;
    private javax.swing.JTable table;
    private javax.swing.JLabel totalKeseluruhan;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the statusPembayaran
     */
    public String getStatusPembayaran() {
        return statusPembayaran;
    }

    /**
     * @param statusPembayaran the statusPembayaran to set
     */
    public void setStatusPembayaran(String statusPembayaran) {
        this.statusPembayaran = statusPembayaran;
    }
}
