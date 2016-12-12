package cm;

use strict;
use warnings;
use Data::Dumper;
use LWP::UserAgent;
use HTTP::Request::Common;

require Exporter;
our @ISA = qw(Exporter);
our @EXPORT = qw();

sub new() {
my $this = shift;
my $config = shift;
my $class = ref($this) || $this;
my $self = {};
    bless($self, $class);
    $self->{debug} = $config->get_debug();
    $self->{log} = $config->get_logfile();

$self->set_header($config->get_id(), $config->get_user(), $config->get_password() );
$self->set_body($config->get_sender(), $config->get_message(), $config->get_telephone(), $config->get_flash() );


    return $self;

}

sub set_header() {
my ($self, $id, $user, $password, $tarif) = @_;
my $header = "";
# Gebruik time since epoch as reference
    if (defined $id and defined $user and defined $password) {
        $header = $header.'<CUSTOMER ID="'.$id.'"/>';
        $header = $header.'<USER LOGIN="'.$user.'" PASSWORD="'.$password.'" />';
        $self->{header} = $header;
        if ($self->{debug}) {
            $self->{log}->print_debug($header);
        }
        return 0;
    } else {
        return 1;
    }
}

sub get_header() {
my ($self) = @_;
    if (defined $self->{header}) {
        return $self->{header};
    } else {
        return "";
    }
}

sub set_body() {
my ($self, $sender, $message, $telephone, $flash) = @_;
my $body = "";
    if (defined $sender and defined $message and defined $telephone) {
	if (defined $flash and $flash) {
	  $body = $body.'<DCS>16</DCS><MESSAGECLASS>0</MESSAGECLASS>';
	}
        $body = $body.'<FROM>'.$sender.'</FROM>';
        $body = $body.'<BODY TYPE="text">'.$message.'</BODY>';
        $body = $body.'<TO>'.$telephone.'</TO>';
        $self->{body} = $body;
        if ($self->{debug}) {
            $self->{log}->print_debug($body);
        }
        return 0;
    } else {
        return 1;
    }
}
# TODO Zelfde structuur als set_header
# Wat doet HEADER optie?
# Wat doet Operator optie?




sub get_body() {
my ($self) = @_;
    if (defined $self->{body}) {
        return $self->{body};
    } else {
        return "";
    }
}

sub set_status() {
my ($self, $response) = @_;
    $self->{status} = $response->as_string;
    return 0;
}

sub get_status() {
my ($self) = @_;
    if (defined $self->{status}) {
        return $self->{status};
    } else {
        return "OK";
    }
}

sub send_http() {
my ($self, $url, $proxyhost, $proxyport) = @_;
my $userAgent = LWP::UserAgent->new(agent => 'sendsms');
if ($proxyhost ne "") {
    $userAgent->proxy(['https'], "$proxyhost\:$proxyport");
    }
else {
    $userAgent->proxy(['https']);
}

my ($response, $xmlmessage);
    $xmlmessage = '<?xml version="1.0" encoding="UTF-8"?><MESSAGES>';
    $xmlmessage = $xmlmessage.$self->get_header();
    $xmlmessage = $xmlmessage.'<MSG>'.$self->get_body().'</MSG>';
    $xmlmessage = $xmlmessage.'</MESSAGES>';
    if (defined $url) {
        $response = $userAgent->request(POST $url, Content_Type => 'text/xml', Content => $xmlmessage);
        if ($self->{debug}) {
          $self->{log}->print_debug($response->as_string());
          $self->{log}->print_debug($response->content());
        }
        $self->set_status($response);
        if ($response->is_success) {
            if ($response->content eq "") {
                # Succesvol gestuurd
                return 0;
            } else {
                # Fout bij CM
                $self->{log}->print_error($response->as_string());
                $self->{log}->print_error($response->content());
                return $response->content;
            }
        } else {
            #Probleem bij versturen naar CM
            $self->{log}->print_error($response->as_string());
            $self->{log}->print_error($response->content());
            return "Error sending to CM: ".$response->code;
        }
        return 0;
    } else {
        return "URL unknown";
    }
}

1;
