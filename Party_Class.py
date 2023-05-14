

class MessangerParty:
    def __init__(self, id_num, name):
        self.id_num = id_num
        self.name = name
        self.rsa_private_key = None
        self.rsa_public_key = None

    def set_keys(self, puk, prk):
        self.rsa_public_key = puk
        self.rsa_private_key = prk
