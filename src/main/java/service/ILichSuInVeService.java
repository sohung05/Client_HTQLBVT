package service;

import entity.LichSuInVe;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ILichSuInVeService extends Remote {
    boolean insert(LichSuInVe ls) throws RemoteException;
    List<LichSuInVe> findByMaVe(String maVe) throws RemoteException;
    int countPrintTimes(String maVe) throws RemoteException;
    LichSuInVe findLastPrint(String maVe) throws RemoteException;
}
