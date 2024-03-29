package com.example.panoramic.Notificacion;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.panoramic.ListarUsuarios;
import com.example.panoramic.MapsUsuario;
import com.example.panoramic.R;
import com.example.panoramic.model.User;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Random;

import io.grpc.internal.JsonParser;


public class Fcm extends FirebaseMessagingService {


    String codigoUsuario;
    User usuario;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.e("token","mi token es:"+s);

    }



    // Se asignan los valores de las variables del titulo, detalle y foto.
    // También se recupera el código de usuario para pasarselo a otras funciones que lo requieran
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String from =remoteMessage.getFrom();

        Log.i("INFO","Se esta haciendo una notificacion");

        if (remoteMessage.getData().size()>0){
            usuario = new User();
            String titulo=remoteMessage.getData().get("titulo");
            String detalle=remoteMessage.getData().get("detalle");
            String foto=remoteMessage.getData().get("foto");
            usuario.setId(remoteMessage.getData().get("UID"));
            // Se verfica si la versión de Android del celular es mayor a oreo.
            mayorqueoreo(titulo,detalle,foto);

        }
    }

    // Verificacion de la versión de Android

    private void mayorqueoreo(String titulo, String detalle, String foto) {
        String id="mensaje";
        NotificationManager nm=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,id);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel nc=new NotificationChannel(id,"nuevo", NotificationManager.IMPORTANCE_HIGH);
            nc.setShowBadge(true);
            assert nm!=null;
            nm.createNotificationChannel(nc);
        }
        try {
            //Bitmap imf_foto= Picasso.get(getApplicationContext()).load(foto).get();
         //   Picasso.get().load(user.getPhotoUrl()).placeholder(R.drawable.iconoperito).into(img);
            Log.i("Info","La url de la foto es"+foto);
            Log.i("Info", "El detalle de la notificación"+ detalle);
            Bitmap imf_foto= Picasso.get().load(foto).get();
            builder.setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(titulo)
                    // Se configura la imagen para las notificaciones
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setContentText(detalle)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(imf_foto).bigLargeIcon(null))
                    .setContentIntent(clicknoti())
                    .setContentInfo("nuevo");

            Random random=new Random();
            int idNotity =random.nextInt(8000);

            assert nm !=null;
            nm.notify(idNotity,builder.build());
        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    // Se indica a que pantalla se debe llevar al usuario cuando se presione la notificacion
    // Por ahora podemos mandarla a la lista de usuarios.
    public PendingIntent clicknoti(){
        Intent nf=new Intent(getApplicationContext(), MapsUsuario.class);
        nf.putExtra("user",usuario);
        nf.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, nf, PendingIntent.FLAG_IMMUTABLE);
    }

}
